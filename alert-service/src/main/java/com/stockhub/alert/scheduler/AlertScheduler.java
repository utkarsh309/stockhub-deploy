package com.stockhub.alert.scheduler;

import com.stockhub.alert.client.AuthClient;
import com.stockhub.alert.client.ProductClient;
import com.stockhub.alert.client.WarehouseClient;
import com.stockhub.alert.dto.AlertRequest;
import com.stockhub.alert.enums.AlertSeverity;
import com.stockhub.alert.enums.AlertType;
import com.stockhub.alert.messaging.AlertPublisher;
import com.stockhub.alert.repository.AlertRepository;
import com.stockhub.alert.service.AlertService;
import com.stockhub.alert.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final AlertService alertService;
    private final AlertRepository alertRepository;
    private final WarehouseClient warehouseClient;
    private final ProductClient productClient;
    private final AuthClient authClient;
    private final EmailService emailService;

    // publisher replaces direct
    // alertService.sendAlert() calls below
    private final AlertPublisher alertPublisher;

//    @Scheduled(fixedDelay = 900000, initialDelay = 15000)
    @Scheduled(fixedDelay = 60000, initialDelay = 5000)
    public void checkLowStock() {
        log.info("=== Low Stock Check Started ===");

        try {
            List<Map<String, Object>> warehouses =
                    warehouseClient.getAllWarehouses();

            if (warehouses == null
                    || warehouses.isEmpty()) {
                log.info("No warehouses found");
                return;
            }

            for (Map<String, Object> warehouse
                    : warehouses) {

                Integer warehouseId =
                        (Integer) warehouse.get(
                                "warehouseId");
                Boolean isActive =
                        (Boolean) warehouse.get(
                                "active");

                if (isActive == null
                        || !isActive) continue;

                List<Map<String, Object>> stockItems =
                        warehouseClient
                                .getStockByWarehouse(
                                        warehouseId);

                if (stockItems == null
                        || stockItems.isEmpty()) continue;

                for (Map<String, Object> stock
                        : stockItems) {

                    Integer productId =
                            (Integer) stock.get(
                                    "productId");
                    Integer quantity =
                            (Integer) stock.get(
                                    "quantity");

                    Map<String, Object> product =
                            getProductSafely(productId);
                    if (product == null) continue;

                    String productName =
                            (String) product.get("name");
                    Integer reorderLevel =
                            (Integer) product.get(
                                    "reorderLevel");
                    Integer maxStockLevel =
                            (Integer) product.get(
                                    "maxStockLevel");
                    Boolean productActive =
                            (Boolean) product.get(
                                    "active");

                    if (productActive == null
                            || !productActive) continue;

                    // Check low stock
                    if (reorderLevel != null
                            && quantity != null
                            && quantity <= reorderLevel) {

                        AlertSeverity severity =
                                getSeverity(quantity,
                                        reorderLevel);

                        createLowStockAlert(
                                productId, productName,
                                warehouseId, quantity,
                                reorderLevel, severity);


                    }

                    // Check overstock
                    if (maxStockLevel != null
                            && quantity != null
                            && quantity > maxStockLevel) {
                        createOverstockAlert(
                                productId, productName,
                                warehouseId, quantity,
                                maxStockLevel);
                    }
                }
            }

            log.info("=== Low Stock Check Completed ===");

        } catch (Exception e) {
            log.error("Stock check failed: {}",
                    e.getMessage());
        }
    }

    // Create Low Stock Alert
    private void createLowStockAlert(
            Integer productId,
            String productName,
            Integer warehouseId,
            Integer quantity,
            Integer reorderLevel,
            AlertSeverity severity) {

        List<Integer> recipients =
                getManagerAndAdminIds();

        for (Integer recipientId : recipients) {

            // Skip if unacknowledged alert already
            // exists for this product+warehouse+recipient
            if (alertRepository
                    .existsByRelatedProductIdAndRelatedWarehouseIdAndAlertTypeAndIsAcknowledgedFalseAndRecipientId(
                            productId, warehouseId,
                            AlertType.LOW_STOCK,
                            recipientId)) {
                log.info("LOW STOCK alert already " +
                                "exists for Product={} " +
                                "Warehouse={} Recipient={}" +
                                ", skipping",
                        productId, warehouseId,
                        recipientId);
                continue;
            }

            AlertRequest alertRequest =
                    new AlertRequest();
            alertRequest.setRecipientId(recipientId);
            alertRequest.setAlertType(
                    AlertType.LOW_STOCK);
            alertRequest.setSeverity(severity);
            alertRequest.setTitle("Low Stock: "
                    + (productName != null
                    ? productName
                    : "Product " + productId));
            alertRequest.setMessage(
                    "Product: "
                            + (productName != null
                            ? productName
                            : "ID " + productId)
                            + " | Current Stock: "
                            + quantity + " units"
                            + " | Reorder Level: "
                            + reorderLevel + " units"
                            + " | Warehouse ID: "
                            + warehouseId
                            + " | Action: Create Purchase Order");
            alertRequest.setRelatedProductId(productId);
            alertRequest.setRelatedWarehouseId(
                    warehouseId);

            try {

                // BEFORE: alertService.sendAlert(alertRequest)
                // direct synchronous call to service
                //
                // AFTER: publish to RabbitMQ queue
                // AlertConsumer picks it up async
                // and calls alertService.sendAlert()
                alertPublisher.publish(alertRequest);

                log.info("LOW STOCK alert published: " +
                                "Product={} Warehouse={} " +
                                "Recipient={} Qty={} " +
                                "ReorderLevel={}",
                        productId, warehouseId,
                        recipientId, quantity,
                        reorderLevel);
            } catch (Exception e) {
                log.error("Failed to publish " +
                        "alert: {}", e.getMessage());
            }
        }
    }

    //  Create Overstock Alert
    private void createOverstockAlert(
            Integer productId,
            String productName,
            Integer warehouseId,
            Integer quantity,
            Integer maxStockLevel) {

        List<Integer> recipients =
                getManagerAndAdminIds();

        for (Integer recipientId : recipients) {

            // Skip if unacknowledged alert already
            // exists for this product+warehouse+recipient
            if (alertRepository
                    .existsByRelatedProductIdAndRelatedWarehouseIdAndAlertTypeAndIsAcknowledgedFalseAndRecipientId(
                            productId, warehouseId,
                            AlertType.OVERSTOCK,
                            recipientId)) {
                log.info("OVERSTOCK alert already " +
                                "exists for Product={} " +
                                "Warehouse={} Recipient={}" +
                                ", skipping",
                        productId, warehouseId,
                        recipientId);
                continue;
            }

            AlertRequest alertRequest =
                    new AlertRequest();
            alertRequest.setRecipientId(recipientId);
            alertRequest.setAlertType(
                    AlertType.OVERSTOCK);
            alertRequest.setSeverity(
                    AlertSeverity.WARNING);
            alertRequest.setTitle("Overstock: "
                    + (productName != null
                    ? productName
                    : "Product " + productId));
            alertRequest.setMessage(
                    "Product: "
                            + (productName != null
                            ? productName
                            : "ID " + productId)
                            + " | Current Stock: "
                            + quantity + " units"
                            + " | Max Stock Level: "
                            + maxStockLevel + " units"
                            + " | Warehouse ID: "
                            + warehouseId
                            + " | Stock exceeds maximum limit!");
            alertRequest.setRelatedProductId(productId);
            alertRequest.setRelatedWarehouseId(
                    warehouseId);

            try {
                // CHANGED
                // BEFORE: alertService.sendAlert(alertRequest)
                // direct synchronous call to service
                //
                // AFTER: publish to RabbitMQ queue
                // AlertConsumer picks it up async
                // and calls alertService.sendAlert()
                alertPublisher.publish(alertRequest);

                log.info("OVERSTOCK alert published: " +
                                "Product={} Warehouse={} " +
                                "Recipient={} Qty={} " +
                                "MaxLevel={}",
                        productId, warehouseId,
                        recipientId, quantity,
                        maxStockLevel);
            } catch (Exception e) {
                log.error("Failed to publish " +
                        "alert: {}", e.getMessage());
            }
        }
    }

    // ─── Get Manager and Admin IDs ─────────────
    // Fetches dynamically from auth-service
    // Falls back to ID 2 if auth-service is down
    private List<Integer> getManagerAndAdminIds() {
        List<Integer> ids = new ArrayList<>();
        try {
            List<Map<String, Object>> managers =
                    authClient.getUsersByRole(
                            "MANAGER");
            List<Map<String, Object>> admins =
                    authClient.getUsersByRole(
                            "ADMIN");
            if (managers != null)
                managers.forEach(u ->
                        ids.add((Integer)
                                u.get("userId")));
            if (admins != null)
                admins.forEach(u ->
                        ids.add((Integer)
                                u.get("userId")));
        } catch (Exception e) {
            log.error("Failed to fetch recipients " +
                            "from auth-service: {}",
                    e.getMessage());
            ids.add(2);
        }
        return ids;
    }

    // ─── Determine Severity ────────────────────
    private AlertSeverity getSeverity(
            Integer quantity,
            Integer reorderLevel) {

        if (quantity == 0)
            return AlertSeverity.CRITICAL;

        double ratio =
                (double) quantity / reorderLevel;

        if (ratio <= 0.3)
            return AlertSeverity.CRITICAL;
        else if (ratio <= 0.6)
            return AlertSeverity.WARNING;
        else
            return AlertSeverity.INFO;
    }

    // ─── Get Product Safely ────────────────────
    private Map<String, Object> getProductSafely(
            Integer productId) {
        try {
            return productClient
                    .getProductById(productId);
        } catch (Exception e) {
            log.error("Failed to get product {}: {}",
                    productId, e.getMessage());
            return null;
        }
    }
}