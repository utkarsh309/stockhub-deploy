package com.stockhub.purchase.dto;

import com.stockhub.purchase.enums.PurchaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class POResponse {

    private Integer poId;
    private Integer supplierId;
    private Integer warehouseId;
    private Integer createdBy;
    private PurchaseStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime expectedDate;
    private LocalDateTime receivedDate;
    private String notes;
    private List<LineItemResponse> lineItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}