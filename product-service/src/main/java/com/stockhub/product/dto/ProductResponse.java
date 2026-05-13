package com.stockhub.product.dto;

import com.stockhub.product.enums.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse implements Serializable {

    private Integer productId;
    private String sku;
    private String name;
    private String description;
    private String category;
    private String brand;
    private UnitOfMeasure unitOfMeasure;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private Integer reorderLevel;
    private Integer maxStockLevel;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}