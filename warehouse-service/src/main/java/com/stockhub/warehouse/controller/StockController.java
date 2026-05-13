package com.stockhub.warehouse.controller;

import com.stockhub.warehouse.dto.*;
import com.stockhub.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final WarehouseService warehouseService;

    // STAFF, MANAGER, ADMIN
    // Add stock (called from purchase-service)
    // Also can be called directly
    @PostMapping("/add")
    public ResponseEntity<StockLevelResponse> add(
            @Valid @RequestBody
            StockUpdateRequest request) {
        return ResponseEntity.ok(
                warehouseService.addStock(request));
    }

    // STAFF, MANAGER, ADMIN
    // Deduct stock when issued or consumed
    @PutMapping("/deduct")
    public ResponseEntity<StockLevelResponse> deduct(
            @Valid @RequestBody
            StockUpdateRequest request) {
        return ResponseEntity.ok(
                warehouseService.deductStock(request));
    }

    // MANAGER, OFFICER, ADMIN
    // Reserve stock for a purchase order
    @PostMapping("/reserve")
    public ResponseEntity<StockLevelResponse> reserve(
            @Valid @RequestBody
            StockUpdateRequest request) {
        return ResponseEntity.ok(
                warehouseService.reserveStock(request));
    }

    // MANAGER, OFFICER, ADMIN
    // Release reserved stock back to available
    @PostMapping("/release")
    public ResponseEntity<StockLevelResponse> release(
            @Valid @RequestBody
            StockUpdateRequest request) {
        return ResponseEntity.ok(
                warehouseService.releaseStock(request));
    }

    // STAFF, MANAGER, ADMIN
    // Transfer stock between warehouses
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @Valid @RequestBody
            StockTransferRequest request) {
        warehouseService.transferStock(request);
        return ResponseEntity.ok(
                "Stock transferred successfully");
    }

    // STAFF, MANAGER, ADMIN
    // Add stock via manual adjustment
    // Example: found extra during cycle count
    @PostMapping("/adjust/add")
    public ResponseEntity<StockLevelResponse>
    adjustAdd(
            @Valid @RequestBody
            StockUpdateRequest request) {
        return ResponseEntity.ok(
                warehouseService.adjustStock(
                        request,
                        "ADJUSTMENT",
                        true)); // true = addition
    }

    // STAFF, MANAGER, ADMIN
    // Deduct stock via manual adjustment
    // Example: discrepancy in cycle count
    @PostMapping("/adjust/deduct")
    public ResponseEntity<StockLevelResponse>
    adjustDeduct(
            @Valid @RequestBody
            StockUpdateRequest request) {
        return ResponseEntity.ok(
                warehouseService.adjustStock(
                        request,
                        "ADJUSTMENT",
                        false)); // false = deduction
    }

    // STAFF, MANAGER, ADMIN
    // Write off damaged or expired stock
    @PostMapping("/write-off")
    public ResponseEntity<StockLevelResponse> writeOff(
            @Valid @RequestBody
            StockUpdateRequest request) {
        return ResponseEntity.ok(
                warehouseService.adjustStock(
                        request,
                        "WRITE_OFF",
                        false)); // always deduction
    }

    // ALL roles
    // Get stock level for product in warehouse
    @GetMapping("/{warehouseId}/{productId}")
    public ResponseEntity<StockLevelResponse>
    getStockLevel(
            @PathVariable Integer warehouseId,
            @PathVariable Integer productId) {
        return ResponseEntity.ok(
                warehouseService.getStockLevel(
                        warehouseId, productId));
    }

    // ALL roles
    // Get all stock items in a warehouse
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<StockLevelResponse>>
    getByWarehouse(
            @PathVariable Integer warehouseId) {
        return ResponseEntity.ok(
                warehouseService
                        .getStockByWarehouse(
                                warehouseId));
    }

    // ALL roles
    // Get stock for one product
    // across all warehouses
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockLevelResponse>>
    getByProduct(
            @PathVariable Integer productId) {
        return ResponseEntity.ok(
                warehouseService
                        .getStockByProduct(productId));
    }

    // MANAGER, ADMIN
    // Get low stock items below reorder level
    @GetMapping("/low-stock")
    public ResponseEntity<List<StockLevelResponse>>
    getLowStock(
            @RequestParam(defaultValue = "10")
            Integer reorderLevel) {
        return ResponseEntity.ok(
                warehouseService
                        .getLowStockItems(reorderLevel));
    }
}