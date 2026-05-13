package com.stockhub.report.service;

import com.stockhub.report.client.MovementClient;
import com.stockhub.report.client.ProductClient;
import com.stockhub.report.client.WarehouseClient;
import com.stockhub.report.dto.ProductMovementReport;
import com.stockhub.report.dto.SnapshotResponse;
import com.stockhub.report.dto.StockValuationResponse;
import com.stockhub.report.entity.InventorySnapshot;
import com.stockhub.report.repository.SnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportServiceImpl
        implements ReportService {

    private final SnapshotRepository snapshotRepository;
    // Feign clients
    private final WarehouseClient warehouseClient;
    private final ProductClient productClient;
    private final MovementClient movementClient;

    // ─── Take Snapshot ─────────────────────────
    // Called by scheduler at midnight daily
    @Override
    public void takeSnapshot() {

        LocalDate today = LocalDate.now();

        // Skip if snapshot already taken today
        if (snapshotRepository.existsBySnapshotDate(today)) {
            log.info("Snapshot already exists for: {}",
                    today);
            return;
        }

        // Get all warehouses via Feign
        List<Map<String, Object>> warehouses =
                warehouseClient.getAllWarehouses();

        // For each warehouse get stock levels
        for (Map<String, Object> warehouse : warehouses) {
            Integer warehouseId = (Integer)
                    warehouse.get("warehouseId");

            // Get all stock in this warehouse
            List<Map<String, Object>> stockItems =
                    warehouseClient.getStockByWarehouse(
                            warehouseId);

            // Create snapshot for each product
            for (Map<String, Object> stock : stockItems) {
                Integer productId = (Integer)
                        stock.get("productId");
                Integer quantity = (Integer)
                        stock.get("quantity");

                // Get cost price from product-service
                Map<String, Object> product =
                        productClient.getProductById(
                                productId);
                // Get cost price as double then convert
                Double costPrice = (Double)
                        product.get("costPrice");

                // Calculate stock value
                BigDecimal stockValue =
                        BigDecimal.valueOf(costPrice)
                                .multiply(BigDecimal
                                        .valueOf(quantity));

                // Save snapshot record
                InventorySnapshot snapshot =
                        InventorySnapshot.builder()
                                .productId(productId)
                                .warehouseId(warehouseId)
                                .quantity(quantity)
                                .stockValue(stockValue)
                                .snapshotDate(today)
                                .build();

                snapshotRepository.save(snapshot);
            }
        }

        log.info("Daily snapshot taken for: {}", today);
    }

    // ─── Get Snapshot By Date ──────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SnapshotResponse> getSnapshotByDate(
            LocalDate date) {
        return snapshotRepository
                .findBySnapshotDate(date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Total Stock Value ─────────────────
    @Override
    @Transactional(readOnly = true)
    public StockValuationResponse getTotalStockValue(
            LocalDate date) {

        // Get total from DB query
        BigDecimal total = snapshotRepository
                .getTotalStockValue(date);

        // Count distinct products
        List<InventorySnapshot> snapshots =
                snapshotRepository
                        .findBySnapshotDate(date);

        return StockValuationResponse.builder()
                .totalValue(total != null
                        ? total : BigDecimal.ZERO)
                .productCount(snapshots.size())
                .calculatedAt(
                        LocalDateTime.now().toString())
                .build();
    }

    // ─── Get Stock Value By Warehouse ──────────
    @Override
    @Transactional(readOnly = true)
    public StockValuationResponse
    getStockValueByWarehouse(
            Integer warehouseId, LocalDate date) {

        BigDecimal total = snapshotRepository
                .getStockValueByWarehouse(
                        warehouseId, date);

        List<InventorySnapshot> snapshots =
                snapshotRepository
                        .findByWarehouseId(warehouseId);

        return StockValuationResponse.builder()
                .totalValue(total != null
                        ? total : BigDecimal.ZERO)
                .productCount(snapshots.size())
                .calculatedAt(
                        LocalDateTime.now().toString())
                .build();
    }

    // ─── Get Top Moving Products ───────────────
    // Products with most movements
    @Override
    @Transactional(readOnly = true)
    public List<ProductMovementReport>
    getTopMovingProducts() {

        // Get all movements from movement-service
        List<Map<String, Object>> allMovements =
                movementClient.getAllMovements();

        // Count movements per product
        Map<Integer, Long> movementCount =
                allMovements.stream()
                        .collect(Collectors.groupingBy(
                                m -> (Integer) m.get("productId"),
                                Collectors.counting()));

        // Sort by count descending and take top 10
        return movementCount.entrySet().stream()
                .sorted(Map.Entry
                        .<Integer, Long>comparingByValue()
                        .reversed())
                .limit(10)
                .map(entry ->
                        ProductMovementReport.builder()
                                .productId(entry.getKey())
                                .totalMovements(
                                        entry.getValue().intValue())
                                .movementCategory("TOP_MOVING")
                                .build())
                .collect(Collectors.toList());
    }

    // ─── Get Slow Moving Products ──────────────
    // Products with less than 5 movements
    @Override
    @Transactional(readOnly = true)
    public List<ProductMovementReport>
    getSlowMovingProducts() {

        List<Map<String, Object>> allMovements =
                movementClient.getAllMovements();

        // Count movements per product
        Map<Integer, Long> movementCount =
                allMovements.stream()
                        .collect(Collectors.groupingBy(
                                m -> (Integer) m.get("productId"),
                                Collectors.counting()));

        // Products with less than 5 movements
        return movementCount.entrySet().stream()
                .filter(entry -> entry.getValue() < 5)
                .map(entry ->
                        ProductMovementReport.builder()
                                .productId(entry.getKey())
                                .totalMovements(
                                        entry.getValue().intValue())
                                .movementCategory("SLOW_MOVING")
                                .build())
                .collect(Collectors.toList());
    }

    // ─── Get Dead Stock ────────────────────────
    // Products with no movement for 90+ days
    @Override
    @Transactional(readOnly = true)
    public List<ProductMovementReport> getDeadStock() {

        List<Map<String, Object>> allMovements =
                movementClient.getAllMovements();

        // Get date 90 days ago
        LocalDate ninetyDaysAgo =
                LocalDate.now().minusDays(90);

        // Group movements by product
        Map<Integer, List<Map<String, Object>>>
                movementsByProduct = allMovements
                .stream()
                .collect(Collectors.groupingBy(
                        m -> (Integer) m.get("productId")));

        List<ProductMovementReport> deadStock =
                new ArrayList<>();

        // Check each product last movement date
        for (Map.Entry<Integer,
                List<Map<String, Object>>> entry
                : movementsByProduct.entrySet()) {

            // Get most recent movement date
            Optional<String> lastMovement =
                    entry.getValue().stream()
                            .map(m -> (String)
                                    m.get("movementDate"))
                            .max(String::compareTo);

            if (lastMovement.isPresent()) {
                // Parse date and check if 90+ days old
                LocalDate lastDate = LocalDate.parse(
                        lastMovement.get()
                                .substring(0, 10));

                if (lastDate.isBefore(ninetyDaysAgo)) {
                    deadStock.add(
                            ProductMovementReport
                                    .builder()
                                    .productId(entry.getKey())
                                    .totalMovements(
                                            entry.getValue()
                                                    .size())
                                    .movementCategory(
                                            "DEAD_STOCK")
                                    .build());
                }
            }
        }

        return deadStock;
    }

    // ─── Get Snapshots Between Dates ───────────
    @Override
    @Transactional(readOnly = true)
    public List<SnapshotResponse> getSnapshotsBetween(
            LocalDate startDate, LocalDate endDate) {
        return snapshotRepository
                .findBySnapshotDateBetween(
                        startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Helper: Map to Response ───────────────
    private SnapshotResponse mapToResponse(
            InventorySnapshot s) {
        return SnapshotResponse.builder()
                .snapshotId(s.getSnapshotId())
                .productId(s.getProductId())
                .warehouseId(s.getWarehouseId())
                .quantity(s.getQuantity())
                .stockValue(s.getStockValue())
                .snapshotDate(s.getSnapshotDate())
                .build();
    }
}