package com.stockhub.purchase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LineItemRequest {

    // Product to order
    @NotNull(message = "Product is required")
    private Integer productId;

    // How many to order
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be atleast 1")
    private Integer quantity;

    // Agreed price per unit
    @NotNull(message = "Unit cost is required")
    private BigDecimal unitCost;
}