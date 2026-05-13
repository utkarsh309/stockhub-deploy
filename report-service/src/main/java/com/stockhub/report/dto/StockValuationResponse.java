package com.stockhub.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockValuationResponse {

    // Total value across all warehouses
    private BigDecimal totalValue;

    // How many products counted
    private Integer productCount;

    // Date of calculation
    private String calculatedAt;
}