package com.stockhub.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMovementReport {

    private Integer productId;
    // Total units moved in period
    private Integer totalMovements;
    // Category fast or slow
    private String movementCategory;
}