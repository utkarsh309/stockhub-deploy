package com.stockhub.product.entity;

import com.stockhub.product.enums.UnitOfMeasure;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    // Unique product code
    @Column(name = "sku",
            nullable = false,
            unique = true)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "brand")
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure",
            nullable = false)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "cost_price",
            nullable = false,
            precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price",
            nullable = false,
            precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    // When stock drops below this → alert
    @Column(name = "reorder_level",
            nullable = false)
    private Integer reorderLevel;

    // When stock exceeds this → alert
    @Column(name = "max_stock_level",
            nullable = false)
    private Integer maxStockLevel;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}