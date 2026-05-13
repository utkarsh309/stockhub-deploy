package com.stockhub.movement.repository;

import com.stockhub.movement.entity.StockMovement;
import com.stockhub.movement.enums.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovementRepository
        extends JpaRepository<StockMovement, Integer> {

    // All movements for a product
    List<StockMovement> findByProductId(
            Integer productId);

    // All movements in a warehouse
    List<StockMovement> findByWarehouseId(
            Integer warehouseId);

    // All movements by type
    List<StockMovement> findByMovementType(
            MovementType movementType);

    // History for specific product in warehouse
    List<StockMovement> findByProductIdAndWarehouseId(
            Integer productId, Integer warehouseId);

    // Movements by reference (PO ID etc)
    List<StockMovement> findByReferenceId(
            Integer referenceId);

    // Movements by user
    List<StockMovement> findByPerformedBy(
            Integer performedBy);
}