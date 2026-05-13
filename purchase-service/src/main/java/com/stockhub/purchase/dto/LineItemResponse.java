package com.stockhub.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineItemResponse {

    private Integer lineItemId;
    private Integer productId;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    // How many received so far
    private Integer receivedQty;
}