package com.stockhub.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockUpdateRequest {

    @NotNull(message = "Warehouse ID is required")
    private Integer warehouseId;

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be atleast 1")
    private Integer quantity;

    // Who is doing this operation
    private Integer performedBy;

    // Reason for adjustment or write off
    private String notes;
}