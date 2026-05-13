package com.stockhub.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO used to call warehouse-service addStock
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockUpdateRequest {

    private Integer warehouseId;
    private Integer productId;
    private Integer quantity;
}