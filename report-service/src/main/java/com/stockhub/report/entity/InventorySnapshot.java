package com.stockhub.report.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_snapshots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySnapshot {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Integer snapshotId;

    // Product this snapshot is for
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    // Warehouse this snapshot is for
    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;

    // Stock quantity at time of snapshot
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Stock value = quantity * costPrice
    @Column(name = "stock_value",
            precision = 10, scale = 2)
    private BigDecimal stockValue;

    // Date of snapshot (daily)
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    // When record was created
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt =
            LocalDateTime.now();
}