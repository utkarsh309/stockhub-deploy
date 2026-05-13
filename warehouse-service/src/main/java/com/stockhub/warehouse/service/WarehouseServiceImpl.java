package com.stockhub.warehouse.service;

import com.stockhub.warehouse.client.MovementClient;
import com.stockhub.warehouse.dto.*;
import com.stockhub.warehouse.entity.StockLevel;
import com.stockhub.warehouse.entity.Warehouse;
import com.stockhub.warehouse.exception.*;
import com.stockhub.warehouse.repository.StockLevelRepository;
import com.stockhub.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WarehouseServiceImpl
        implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    // Feign client to record movements
    private final MovementClient movementClient;

    // ─── Create Warehouse ──────────────────────
    @Override
    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseResponse createWarehouse(
            WarehouseRequest request) {
        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .address(request.getAddress())
                .managerId(request.getManagerId())
                .capacity(request.getCapacity())
                .phone(request.getPhone())
                .build();
        Warehouse saved =
                warehouseRepository.save(warehouse);
        log.info("Warehouse created: {}",
                saved.getName());
        return mapToWarehouseResponse(saved);
    }

    // ─── Get Warehouse By ID ───────────────────
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouses", key = "#warehouseId")
    public WarehouseResponse getWarehouseById(
            Integer warehouseId) {
        return mapToWarehouseResponse(
                findWarehouseById(warehouseId));
    }

    // ─── Get All Warehouses ────────────────────
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouses", key = "'all'")
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository
                .findByIsActiveTrue()
                .stream()
                .map(this::mapToWarehouseResponse)
                .collect(Collectors.toList());
    }

    // ─── Update Warehouse ──────────────────────
    @Override
    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseResponse updateWarehouse(
            Integer warehouseId,
            WarehouseRequest request) {
        Warehouse warehouse =
                findWarehouseById(warehouseId);
        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setAddress(request.getAddress());
        warehouse.setManagerId(request.getManagerId());
        warehouse.setCapacity(request.getCapacity());
        warehouse.setPhone(request.getPhone());
        return mapToWarehouseResponse(
                warehouseRepository.save(warehouse));
    }

    // ─── Deactivate Warehouse ──────────────────
    @Override
    @CacheEvict(value = "warehouses", allEntries = true)
    public void deactivateWarehouse(
            Integer warehouseId) {
        Warehouse warehouse =
                findWarehouseById(warehouseId);
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
        log.info("Warehouse deactivated: {}",
                warehouse.getName());
    }

    // ─── Activate Warehouse ────────────────────
    @Override
    @CacheEvict(value = "warehouses", allEntries = true)
    public void activateWarehouse(
            Integer warehouseId) {
        Warehouse warehouse =
                findWarehouseById(warehouseId);
        warehouse.setActive(true);
        warehouseRepository.save(warehouse);
        log.info("Warehouse activated: {}",
                warehouse.getName());
    }

    // ─── Add Stock ─────────────────────────────
// Used internally by transfer and purchase
// Movement recorded by the caller
    @Override
    @CacheEvict(value = "stock", allEntries = true)
    public StockLevelResponse addStock(
            StockUpdateRequest request) {

        // Find existing stock or create new record
        StockLevel stock = stockLevelRepository
                .findByWarehouseIdAndProductId(
                        request.getWarehouseId(),
                        request.getProductId())
                .orElse(StockLevel.builder()
                        .warehouseId(
                                request.getWarehouseId())
                        .productId(
                                request.getProductId())
                        .quantity(0)
                        .reservedQuantity(0)
                        .build());

        // Add quantity to current stock
        stock.setQuantity(
                stock.getQuantity()
                        + request.getQuantity());
        stock.setLastUpdated(LocalDateTime.now());

        log.info("Stock added: {} units " +
                        "Product: {} Warehouse: {}",
                request.getQuantity(),
                request.getProductId(),
                request.getWarehouseId());

        StockLevel saved = stockLevelRepository.save(stock);

        // Record STOCK_IN only when called directly
        // (performedBy is null when called internally
        // by transfer or purchase-service)
        if (request.getPerformedBy() != null) {
            MovementRequest movement = new MovementRequest(
                    request.getProductId(),
                    request.getWarehouseId(),
                    "STOCK_IN",
                    request.getQuantity(),
                    saved.getQuantity(),
                    null,
                    "MANUAL",
                    request.getPerformedBy(),
                    request.getNotes());
            movementClient.recordMovement(movement);
        }

        return mapToStockResponse(saved);
    }

    // ─── Deduct Stock ──────────────────────────
// Used internally by transfer
// Movement recorded by the caller
    @Override
    @CacheEvict(value = "stock", allEntries = true)
    public StockLevelResponse deductStock(
            StockUpdateRequest request) {

        StockLevel stock = findStockLevel(
                request.getWarehouseId(),
                request.getProductId());

        // Check available quantity before deducting
        if (stock.getAvailableQuantity()
                < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: "
                            + stock.getAvailableQuantity()
                            + " Requested: "
                            + request.getQuantity());
        }

        // Deduct from total quantity
        stock.setQuantity(
                stock.getQuantity()
                        - request.getQuantity());
        stock.setLastUpdated(LocalDateTime.now());

        log.info("Stock deducted: {} units " +
                        "Product: {} Warehouse: {}",
                request.getQuantity(),
                request.getProductId(),
                request.getWarehouseId());

        StockLevel saved = stockLevelRepository.save(stock);

        // Record STOCK_OUT only when called directly
        // (performedBy is null when called internally
        // by transfer or purchase-service)
        if (request.getPerformedBy() != null) {
            MovementRequest movement = new MovementRequest(
                    request.getProductId(),
                    request.getWarehouseId(),
                    "STOCK_OUT",
                    request.getQuantity(),
                    saved.getQuantity(),
                    null,
                    "MANUAL",
                    request.getPerformedBy(),
                    request.getNotes());
            movementClient.recordMovement(movement);
        }

        return mapToStockResponse(saved);
    }

    // ─── Reserve Stock ─────────────────────────
    // Marks stock as reserved for pending orders
    // No movement recorded - just reservation
    @Override
    @CacheEvict(value = "stock", allEntries = true)
    public StockLevelResponse reserveStock(
            StockUpdateRequest request) {

        StockLevel stock = findStockLevel(
                request.getWarehouseId(),
                request.getProductId());

        // Check available quantity to reserve
        if (stock.getAvailableQuantity()
                < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock to reserve. " +
                            "Available: "
                            + stock.getAvailableQuantity());
        }

        // Increase reserved quantity
        stock.setReservedQuantity(
                stock.getReservedQuantity()
                        + request.getQuantity());
        stock.setLastUpdated(LocalDateTime.now());

        return mapToStockResponse(
                stockLevelRepository.save(stock));
    }

    // ─── Release Stock ─────────────────────────
    // Releases reserved stock back to available
    // No movement recorded - just release
    @Override
    @CacheEvict(value = "stock", allEntries = true)
    public StockLevelResponse releaseStock(
            StockUpdateRequest request) {

        StockLevel stock = findStockLevel(
                request.getWarehouseId(),
                request.getProductId());

        // Reduce reserved but never below 0
        int newReserved =
                stock.getReservedQuantity()
                        - request.getQuantity();
        stock.setReservedQuantity(
                Math.max(0, newReserved));
        stock.setLastUpdated(LocalDateTime.now());

        return mapToStockResponse(
                stockLevelRepository.save(stock));
    }

    // ─── Transfer Stock ────────────────────────
    // Moves stock between warehouses
    // Records TRANSFER_OUT and TRANSFER_IN
    // Both in same @Transactional
    // If one fails BOTH rollback ✅
    @Override
    @CacheEvict(value = "stock", allEntries = true)
    public void transferStock(
            StockTransferRequest request) {

        // Source and destination must be different
        if (request.getSourceWarehouseId()
                .equals(request
                        .getDestinationWarehouseId())) {
            throw new InsufficientStockException(
                    "Source and destination " +
                            "cannot be same warehouse");
        }

        // Step 1: Deduct from source warehouse
        StockUpdateRequest deduct =
                new StockUpdateRequest();
        deduct.setWarehouseId(
                request.getSourceWarehouseId());
        deduct.setProductId(request.getProductId());
        deduct.setQuantity(request.getQuantity());
        StockLevelResponse sourceStock =
                deductStock(deduct);

        // Step 2: Add to destination warehouse
        StockUpdateRequest add =
                new StockUpdateRequest();
        add.setWarehouseId(
                request.getDestinationWarehouseId());
        add.setProductId(request.getProductId());
        add.setQuantity(request.getQuantity());
        StockLevelResponse destStock =
                addStock(add);

        // Step 3: Record TRANSFER_OUT movement
        // for source warehouse
        MovementRequest transferOut =
                new MovementRequest(
                        request.getProductId(),
                        request.getSourceWarehouseId(),
                        "TRANSFER_OUT",
                        request.getQuantity(),
                        // Balance after deduction
                        sourceStock.getQuantity(),
                        null,
                        "TRANSFER",
                        request.getPerformedBy(),
                        "Transfer to warehouse: "
                                + request
                                .getDestinationWarehouseId());
        movementClient.recordMovement(transferOut);

        // Step 4: Record TRANSFER_IN movement
        // for destination warehouse
        MovementRequest transferIn =
                new MovementRequest(
                        request.getProductId(),
                        request.getDestinationWarehouseId(),
                        "TRANSFER_IN",
                        request.getQuantity(),
                        // Balance after addition
                        destStock.getQuantity(),
                        null,
                        "TRANSFER",
                        request.getPerformedBy(),
                        "Transfer from warehouse: "
                                + request
                                .getSourceWarehouseId());
        movementClient.recordMovement(transferIn);

        log.info("Stock transferred: {} units " +
                        "Product: {} From: {} To: {}",
                request.getQuantity(),
                request.getProductId(),
                request.getSourceWarehouseId(),
                request.getDestinationWarehouseId());
    }

    // ─── Adjust Stock ──────────────────────────
    // Manual correction or write off
    // Records ADJUSTMENT or WRITE_OFF movement
    @Override
    @CacheEvict(value = "stock", allEntries = true)
    public StockLevelResponse adjustStock(
            StockUpdateRequest request,
            String movementType,
            boolean isAddition) {

        StockLevel stock;

        if (isAddition) {
            // Adding stock positive adjustment
            stock = stockLevelRepository
                    .findByWarehouseIdAndProductId(
                            request.getWarehouseId(),
                            request.getProductId())
                    .orElse(StockLevel.builder()
                            .warehouseId(
                                    request.getWarehouseId())
                            .productId(
                                    request.getProductId())
                            .quantity(0)
                            .reservedQuantity(0)
                            .build());

            stock.setQuantity(
                    stock.getQuantity()
                            + request.getQuantity());
        } else {
            // Deducting stock write off or damage
            stock = findStockLevel(
                    request.getWarehouseId(),
                    request.getProductId());

            // Check enough available to remove
            if (stock.getAvailableQuantity()
                    < request.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock. Available: "
                                + stock.getAvailableQuantity());
            }

            stock.setQuantity(
                    stock.getQuantity()
                            - request.getQuantity());
        }

        stock.setLastUpdated(LocalDateTime.now());
        StockLevel saved =
                stockLevelRepository.save(stock);

        // Record ADJUSTMENT or WRITE_OFF movement
        MovementRequest movementRequest =
                new MovementRequest(
                        request.getProductId(),
                        request.getWarehouseId(),
                        movementType,
                        request.getQuantity(),
                        // Balance after adjustment
                        saved.getQuantity(),
                        null,
                        movementType,
                        request.getPerformedBy(),
                        request.getNotes());
        movementClient.recordMovement(movementRequest);

        log.info("{}: {} units Product: {} " +
                        "Warehouse: {}",
                movementType,
                request.getQuantity(),
                request.getProductId(),
                request.getWarehouseId());

        return mapToStockResponse(saved);
    }

    // ─── Get Stock Level ───────────────────────
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "stock", key = "#warehouseId + ':' + #productId")
    public StockLevelResponse getStockLevel(
            Integer warehouseId,
            Integer productId) {
        return mapToStockResponse(
                findStockLevel(warehouseId,
                        productId));
    }

    // ─── Get Stock By Warehouse ────────────────
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "stock", key = "'warehouse:' + #warehouseId")
    public List<StockLevelResponse> getStockByWarehouse(
            Integer warehouseId) {
        return stockLevelRepository
                .findByWarehouseId(warehouseId)
                .stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Stock By Product ──────────────────
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "stock", key = "'product:' + #productId")
    public List<StockLevelResponse> getStockByProduct(
            Integer productId) {
        return stockLevelRepository
                .findByProductId(productId)
                .stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Low Stock Items ───────────────────
    @Override
    @Transactional(readOnly = true)
    public List<StockLevelResponse> getLowStockItems(
            Integer reorderLevel) {
        return stockLevelRepository
                .findLowStockItems(reorderLevel)
                .stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    // ─── Helper: Find Warehouse ────────────────
    private Warehouse findWarehouseById(
            Integer warehouseId) {
        return warehouseRepository
                .findById(warehouseId)
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse not found: "
                                        + warehouseId));
    }

    // ─── Helper: Find Stock Level ──────────────
    private StockLevel findStockLevel(
            Integer warehouseId,
            Integer productId) {
        return stockLevelRepository
                .findByWarehouseIdAndProductId(
                        warehouseId, productId)
                .orElseThrow(() ->
                        new StockNotFoundException(
                                "Stock not found for " +
                                        "product " + productId +
                                        " in warehouse "
                                        + warehouseId));
    }

    // ─── Helper: Map Warehouse to Response ─────
    private WarehouseResponse mapToWarehouseResponse(
            Warehouse w) {
        return WarehouseResponse.builder()
                .warehouseId(w.getWarehouseId())
                .name(w.getName())
                .location(w.getLocation())
                .address(w.getAddress())
                .managerId(w.getManagerId())
                .capacity(w.getCapacity())
                .phone(w.getPhone())
                .isActive(w.isActive())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    // ─── Helper: Map Stock to Response ─────────
    private StockLevelResponse mapToStockResponse(
            StockLevel s) {
        return StockLevelResponse.builder()
                .stockId(s.getStockId())
                .warehouseId(s.getWarehouseId())
                .productId(s.getProductId())
                .quantity(s.getQuantity())
                .reservedQuantity(
                        s.getReservedQuantity())
                .availableQuantity(
                        s.getAvailableQuantity())
                .lastUpdated(s.getLastUpdated())
                .build();
    }
}