package com.stockhub.purchase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoodsReceiptRequest {

    // Which line item is being received
    @NotNull(message = "Line item ID is required")
    private Integer lineItemId;

    // How many units received now
    @NotNull(message = "Received quantity is required")
    @Min(value = 1, message = "Quantity must be atleast 1")
    private Integer receivedQty;
}