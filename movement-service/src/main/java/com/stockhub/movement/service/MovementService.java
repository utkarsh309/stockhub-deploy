package com.stockhub.movement.service;

import com.stockhub.movement.dto.MovementRequest;
import com.stockhub.movement.dto.MovementResponse;
import com.stockhub.movement.enums.MovementType;
import java.util.List;

public interface MovementService {

    // Record new movement (immutable)
    MovementResponse recordMovement(
            MovementRequest request);

    // Get movement by ID
    MovementResponse getMovementById(
            Integer movementId);

    // Get all movements for a product
    List<MovementResponse> getByProduct(
            Integer productId);

    // Get all movements in a warehouse
    List<MovementResponse> getByWarehouse(
            Integer warehouseId);

    // Get movements by type
    List<MovementResponse> getByType(
            MovementType movementType);

    // Get history for product in warehouse
    List<MovementResponse> getHistory(
            Integer productId, Integer warehouseId);

    // Get movements by reference
    List<MovementResponse> getByReference(
            Integer referenceId);

    // Get all movements
    List<MovementResponse> getAllMovements();
}