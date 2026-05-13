package com.stockhub.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockTransferRequest {

    @NotNull(message = "Source warehouse is required")
    private Integer sourceWarehouseId;

    @NotNull(message = "Destination warehouse is required")
    private Integer destinationWarehouseId;

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be atleast 1")
    private Integer quantity;

    // Who is doing this transfer
    @NotNull(message = "Performed by is required")
    private Integer performedBy;

    private String notes;
}