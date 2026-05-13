package com.stockhub.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO used to call movement-service
// to record STOCK_IN when goods received
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovementRequest {

    private Integer productId;
    private Integer warehouseId;
    // Movement type e.g STOCK_IN
    private String movementType;
    private Integer quantity;
    // Stock balance after this movement
    private Integer balanceAfter;
    // PO ID as reference
    private Integer referenceId;
    // Reference type PO
    private String referenceType;
    // User who received goods
    private Integer performedBy;
    private String notes;
}