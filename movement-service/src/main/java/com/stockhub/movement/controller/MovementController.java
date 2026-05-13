package com.stockhub.movement.controller;

import com.stockhub.movement.dto.MovementRequest;
import com.stockhub.movement.dto.MovementResponse;
import com.stockhub.movement.enums.MovementType;
import com.stockhub.movement.service.MovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class MovementController {

    private final MovementService movementService;

    // Called by other services to record movement
    @PostMapping
    public ResponseEntity<MovementResponse> record(
            @Valid @RequestBody MovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movementService
                        .recordMovement(request));
    }

    // ALL roles - Get movement by ID
    @GetMapping("/{movementId}")
    public ResponseEntity<MovementResponse> getById(
            @PathVariable Integer movementId) {
        return ResponseEntity.ok(
                movementService
                        .getMovementById(movementId));
    }

    // ALL roles - All movements for product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<MovementResponse>>
    getByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(
                movementService.getByProduct(productId));
    }

    // ALL roles - All movements in warehouse
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<MovementResponse>>
    getByWarehouse(
            @PathVariable Integer warehouseId) {
        return ResponseEntity.ok(
                movementService
                        .getByWarehouse(warehouseId));
    }

    // MANAGER, ADMIN - Get by movement type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<MovementResponse>>
    getByType(@PathVariable MovementType type) {
        return ResponseEntity.ok(
                movementService.getByType(type));
    }

    // ALL roles - Product history in warehouse
    @GetMapping("/history")
    public ResponseEntity<List<MovementResponse>>
    getHistory(
            @RequestParam Integer productId,
            @RequestParam Integer warehouseId) {
        return ResponseEntity.ok(
                movementService.getHistory(
                        productId, warehouseId));
    }

    // ALL roles - Movements by reference
    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<List<MovementResponse>>
    getByReference(
            @PathVariable Integer referenceId) {
        return ResponseEntity.ok(
                movementService
                        .getByReference(referenceId));
    }

    // MANAGER, ADMIN - All movements
    @GetMapping
    public ResponseEntity<List<MovementResponse>>
    getAll() {
        return ResponseEntity.ok(
                movementService.getAllMovements());
    }
}