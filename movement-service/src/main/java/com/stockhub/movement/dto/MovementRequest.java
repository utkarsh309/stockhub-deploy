package com.stockhub.movement.dto;

import com.stockhub.movement.enums.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovementRequest {

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Warehouse ID is required")
    private Integer warehouseId;

    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be atleast 1")
    private Integer quantity;

    // Stock balance after this movement
    @NotNull(message = "Balance after is required")
    private Integer balanceAfter;

    // Optional reference to PO or transfer
    private Integer referenceId;
    private String referenceType;

    @NotNull(message = "Performed by is required")
    private Integer performedBy;

    private String notes;
}