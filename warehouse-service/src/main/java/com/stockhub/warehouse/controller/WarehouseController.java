package com.stockhub.warehouse.controller;

import com.stockhub.warehouse.dto.WarehouseRequest;
import com.stockhub.warehouse.dto.WarehouseResponse;
import com.stockhub.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    // ADMIN only
    @PostMapping
    public ResponseEntity<WarehouseResponse> create(
            @Valid @RequestBody
            WarehouseRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(warehouseService
                        .createWarehouse(request));
    }

    // ALL roles
    @GetMapping
    public ResponseEntity<List<WarehouseResponse>>
    getAll() {
        return ResponseEntity.ok(
                warehouseService.getAllWarehouses());
    }

    // ALL roles
    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponse> getById(
            @PathVariable Integer warehouseId) {
        return ResponseEntity.ok(
                warehouseService
                        .getWarehouseById(warehouseId));
    }

    // ADMIN only
    @PutMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponse> update(
            @PathVariable Integer warehouseId,
            @Valid @RequestBody
            WarehouseRequest request) {
        return ResponseEntity.ok(
                warehouseService.updateWarehouse(
                        warehouseId, request));
    }

    // ADMIN only
    @PutMapping("/{warehouseId}/deactivate")
    public ResponseEntity<String> deactivate(
            @PathVariable Integer warehouseId) {
        warehouseService.deactivateWarehouse(
                warehouseId);
        return ResponseEntity.ok(
                "Warehouse deactivated successfully");
    }

    // ADMIN only
    @PutMapping("/{warehouseId}/activate")
    public ResponseEntity<String> activate(
            @PathVariable Integer warehouseId) {
        warehouseService.activateWarehouse(warehouseId);
        return ResponseEntity.ok(
                "Warehouse activated successfully");
    }
}