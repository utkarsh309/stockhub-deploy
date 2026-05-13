package com.stockhub.product.dto;

import com.stockhub.product.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String category;
    private String brand;

    @NotNull(message = "Unit of measure is required")
    private UnitOfMeasure unitOfMeasure;

    @NotNull(message = "Cost price is required")
    private BigDecimal costPrice;

    @NotNull(message = "Selling price is required")
    private BigDecimal sellingPrice;

    @NotNull(message = "Reorder level is required")
    private Integer reorderLevel;

    @NotNull(message = "Max stock level is required")
    private Integer maxStockLevel;
}