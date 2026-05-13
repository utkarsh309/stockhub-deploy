package com.stockhub.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotResponse {

    private Integer snapshotId;
    private Integer productId;
    private Integer warehouseId;
    private Integer quantity;
    private BigDecimal stockValue;
    private LocalDate snapshotDate;
}