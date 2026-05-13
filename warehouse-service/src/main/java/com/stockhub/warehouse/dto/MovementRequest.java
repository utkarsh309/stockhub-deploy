package com.stockhub.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO used to call movement-service
// to record stock movements
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovementRequest {

    // Product that moved
    private Integer productId;

    // Warehouse where movement happened
    private Integer warehouseId;

    // Type: TRANSFER_OUT, TRANSFER_IN,
    //       ADJUSTMENT, WRITE_OFF
    private String movementType;

    // How many units moved
    private Integer quantity;

    // Stock balance after movement
    private Integer balanceAfter;

    // Reference ID (transfer ID etc)
    private Integer referenceId;

    // Reference type (TRANSFER, ADJUSTMENT)
    private String referenceType;

    // User who performed movement
    private Integer performedBy;

    // Optional notes
    private String notes;
}