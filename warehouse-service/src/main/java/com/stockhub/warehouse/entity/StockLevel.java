package com.stockhub.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_levels",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"warehouse_id", "product_id"}
        ))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Integer stockId;

    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;

    // Product ID from product-service
    // No FK because different service
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    // Total stock in warehouse
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    // Reserved for pending orders
    @Column(name = "reserved_quantity",
            nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Available = Total - Reserved
    // Not stored in DB, calculated on the fly
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}