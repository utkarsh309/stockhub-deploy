package com.stockhub.alert.scheduler;

import com.stockhub.alert.client.AuthClient;
import com.stockhub.alert.client.ProductClient;
import com.stockhub.alert.client.WarehouseClient;
import com.stockhub.alert.enums.AlertSeverity;
import com.stockhub.alert.enums.AlertType;
import com.stockhub.alert.messaging.AlertPublisher;
import com.stockhub.alert.repository.AlertRepository;
import com.stockhub.alert.service.AlertService;
import com.stockhub.alert.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertSchedulerTest {

    @Mock private AlertService alertService;
    @Mock private AlertRepository alertRepository;
    @Mock private WarehouseClient warehouseClient;
    @Mock private ProductClient productClient;
    @Mock private AuthClient authClient;
    @Mock private EmailService emailService;
    @Mock private AlertPublisher alertPublisher;

    @InjectMocks private AlertScheduler alertScheduler;

    // ─── checkLowStock ─────────────────────────

    @Test
    void checkLowStock_noWarehouses_skips() {
        when(warehouseClient.getAllWarehouses()).thenReturn(List.of());

        alertScheduler.checkLowStock();

        verify(alertPublisher, never()).publish(any());
    }

    @Test
    void checkLowStock_nullWarehouses_skips() {
        when(warehouseClient.getAllWarehouses()).thenReturn(null);

        alertScheduler.checkLowStock();

        verify(alertPublisher, never()).publish(any());
    }

    @Test
    void checkLowStock_inactiveWarehouse_skips() {
        Map<String, Object> warehouse = Map.of("warehouseId", 1, "active", false);
        when(warehouseClient.getAllWarehouses()).thenReturn(List.of(warehouse));

        alertScheduler.checkLowStock();

        verify(warehouseClient, never()).getStockByWarehouse(anyInt());
    }

    @Test
    void checkLowStock_emptyStock_skips() {
        Map<String, Object> warehouse = Map.of("warehouseId", 1, "active", true);
        when(warehouseClient.getAllWarehouses()).thenReturn(List.of(warehouse));
        when(warehouseClient.getStockByWarehouse(1)).thenReturn(List.of());

        alertScheduler.checkLowStock();

        verify(alertPublisher, never()).publish(any());
    }

    @Test
    void checkLowStock_productFetchFails_skips() {
        Map<String, Object> warehouse = Map.of("warehouseId", 1, "active", true);
        Map<String, Object> stock = Map.of("productId", 1, "quantity", 5);
        when(warehouseClient.getAllWarehouses()).thenReturn(List.of(warehouse));
        when(warehouseClient.getStockByWarehouse(1)).thenReturn(List.of(stock));
        when(productClient.getProductById(1)).thenThrow(new RuntimeException("service down"));

        alertScheduler.checkLowStock();

        verify(alertPublisher, never()).publish(any());
    }

    @Test
    void checkLowStock_inactiveProduct_skips() {
        Map<String, Object> warehouse = Map.of("warehouseId", 1, "active", true);
        Map<String, Object> stock = Map.of("productId", 1, "quantity", 5);
        Map<String, Object> product = Map.of(
                "name", "Laptop", "reorderLevel", 10,
                "maxStockLevel", 100, "active", false);

        when(warehouseClient.getAllWarehouses()).thenReturn(List.of(warehouse));
        when(warehouseClient.getStockByWarehouse(1)).thenReturn(List.of(stock));
        when(productClient.getProductById(1)).thenReturn(product);

        alertScheduler.checkLowStock();

        verify(alertPublisher, never()).publish(any());
    }

    @Test
    void checkLowStock_lowStock_publishesAlert() {
        Map<String, Object> warehouse = Map.of("warehouseId", 1, "active", true);
        Map<String, Object> stock = Map.of("productId", 1, "quantity", 5);
        Map<String, Object> product = Map.of(
                "name", "Laptop", "reorderLevel", 10,
                "maxStockLevel", 100, "active", true);
        Map<String, Object> admin = Map.of("userId", 2);

        when(warehouseClient.getAllWarehouses()).thenReturn(List.of(warehouse));
        when(warehouseClient.getStockByWarehouse(1)).thenReturn(List.of(stock));
        when(productClient.getProductById(1)).thenReturn(product);
        when(authClient.getUsersByRole("MANAGER")).thenReturn(List.of());
        when(authClient.getUsersByRole("ADMIN")).thenReturn(List.of(admin));
        when(alertRepository
                .existsByRelatedProductIdAndRelatedWarehouseIdAndAlertTypeAndIsAcknowledgedFalseAndRecipientId(
                        anyInt(), anyInt(), any(), anyInt()))
                .thenReturn(false);

        alertScheduler.checkLowStock();

        verify(alertPublisher).publish(any());
    }

    @Test
    void checkLowStock_alertAlreadyExists_skipsPublish() {
        Map<String, Object> warehouse = Map.of("warehouseId", 1, "active", true);
        Map<String, Object> stock = Map.of("productId", 1, "quantity", 5);
        Map<String, Object> product = Map.of(
                "name", "Laptop", "reorderLevel", 10,
                "maxStockLevel", 100, "active", true);
        Map<String, Object> admin = Map.of("userId", 2);

        when(warehouseClient.getAllWarehouses()).thenReturn(List.of(warehouse));
        when(warehouseClient.getStockByWarehouse(1)).thenReturn(List.of(stock));
        when(productClient.getProductById(1)).thenReturn(product);
        when(authClient.getUsersByRole("MANAGER")).thenReturn(List.of());
        when(authClient.getUsersByRole("ADMIN")).thenReturn(List.of(admin));
        when(alertRepository
                .existsByRelatedProductIdAndRelatedWarehouseIdAndAlertTypeAndIsAcknowledgedFalseAndRecipientId(
                        anyInt(), anyInt(), eq(AlertType.LOW_STOCK), anyInt()))
                .thenReturn(true);

        alertScheduler.checkLowStock();

        verify(alertPublisher, never()).publish(any());
    }

    @Test
    void checkLowStock_overstock_publishesAlert() {
        Map<String, Object> warehouse = Map.of("warehouseId", 1, "active", true);
        // quantity(200) > maxStockLevel(100) → overstock
        Map<String, Object> stock = Map.of("productId", 1, "quantity", 200);
        Map<String, Object> product = Map.of(
                "name", "Laptop", "reorderLevel", 10,
                "maxStockLevel", 100, "active", true);
        Map<String, Object> admin = Map.of("userId", 2);

        when(warehouseClient.getAllWarehouses()).thenReturn(List.of(warehouse));
        when(warehouseClient.getStockByWarehouse(1)).thenReturn(List.of(stock));
        when(productClient.getProductById(1)).thenReturn(product);
        when(authClient.getUsersByRole("MANAGER")).thenReturn(List.of());
        when(authClient.getUsersByRole("ADMIN")).thenReturn(List.of(admin));
        when(alertRepository
                .existsByRelatedProductIdAndRelatedWarehouseIdAndAlertTypeAndIsAcknowledgedFalseAndRecipientId(
                        anyInt(), anyInt(), eq(AlertType.OVERSTOCK), anyInt()))
                .thenReturn(false);

        alertScheduler.checkLowStock();

        verify(alertPublisher).publish(any());
    }

    @Test
    void checkLowStock_authServiceDown_usesDefaultRecipient() {
        Map<String, Object> warehouse = Map.of("warehouseId", 1, "active", true);
        Map<String, Object> stock = Map.of("productId", 1, "quantity", 3);
        Map<String, Object> product = Map.of(
                "name", "Laptop", "reorderLevel", 10,
                "maxStockLevel", 100, "active", true);

        when(warehouseClient.getAllWarehouses()).thenReturn(List.of(warehouse));
        when(warehouseClient.getStockByWarehouse(1)).thenReturn(List.of(stock));
        when(productClient.getProductById(1)).thenReturn(product);
        when(authClient.getUsersByRole(anyString()))
                .thenThrow(new RuntimeException("auth-service down"));
        when(alertRepository
                .existsByRelatedProductIdAndRelatedWarehouseIdAndAlertTypeAndIsAcknowledgedFalseAndRecipientId(
                        anyInt(), anyInt(), any(), anyInt()))
                .thenReturn(false);

        alertScheduler.checkLowStock();

        // fallback recipient id=2 is used
        verify(alertPublisher).publish(any());
    }

    @Test
    void checkLowStock_warehouseClientThrows_doesNotPropagate() {
        when(warehouseClient.getAllWarehouses())
                .thenThrow(new RuntimeException("connection refused"));

        // should not throw
        alertScheduler.checkLowStock();

        verify(alertPublisher, never()).publish(any());
    }

    @Test
    void checkLowStock_zeroQuantity_criticalSeverity() {
        Map<String, Object> warehouse = Map.of("warehouseId", 1, "active", true);
        Map<String, Object> stock = Map.of("productId", 1, "quantity", 0);
        Map<String, Object> product = Map.of(
                "name", "Laptop", "reorderLevel", 10,
                "maxStockLevel", 100, "active", true);
        Map<String, Object> admin = Map.of("userId", 2);

        when(warehouseClient.getAllWarehouses()).thenReturn(List.of(warehouse));
        when(warehouseClient.getStockByWarehouse(1)).thenReturn(List.of(stock));
        when(productClient.getProductById(1)).thenReturn(product);
        when(authClient.getUsersByRole("MANAGER")).thenReturn(List.of());
        when(authClient.getUsersByRole("ADMIN")).thenReturn(List.of(admin));
        when(alertRepository
                .existsByRelatedProductIdAndRelatedWarehouseIdAndAlertTypeAndIsAcknowledgedFalseAndRecipientId(
                        anyInt(), anyInt(), any(), anyInt()))
                .thenReturn(false);

        alertScheduler.checkLowStock();

        verify(alertPublisher).publish(argThat(req ->
                req.getSeverity() == AlertSeverity.CRITICAL));
    }

    @Test
    void checkLowStock_nullProductName_usesProductId() {
        Map<String, Object> warehouse = Map.of("warehouseId", 1, "active", true);
        Map<String, Object> stock = Map.of("productId", 1, "quantity", 2);
        // name is null
        Map<String, Object> product = new java.util.HashMap<>();
        product.put("name", null);
        product.put("reorderLevel", 10);
        product.put("maxStockLevel", 100);
        product.put("active", true);
        Map<String, Object> admin = Map.of("userId", 2);

        when(warehouseClient.getAllWarehouses()).thenReturn(List.of(warehouse));
        when(warehouseClient.getStockByWarehouse(1)).thenReturn(List.of(stock));
        when(productClient.getProductById(1)).thenReturn(product);
        when(authClient.getUsersByRole("MANAGER")).thenReturn(List.of());
        when(authClient.getUsersByRole("ADMIN")).thenReturn(List.of(admin));
        when(alertRepository
                .existsByRelatedProductIdAndRelatedWarehouseIdAndAlertTypeAndIsAcknowledgedFalseAndRecipientId(
                        anyInt(), anyInt(), any(), anyInt()))
                .thenReturn(false);

        alertScheduler.checkLowStock();

        verify(alertPublisher).publish(argThat(req ->
                req.getTitle().contains("Product 1")));
    }
}