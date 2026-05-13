package com.stockhub.purchase.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "po_line_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class POLineItem {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_item_id")
    private Integer lineItemId;

    // Link to parent PO
    // ManyToOne = many items belong to one PO
    @ManyToOne
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    // Product from product-service
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    // How many units ordered
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Price per unit at time of ordering
    @Column(name = "unit_cost",
            nullable = false,
            precision = 10, scale = 2)
    private BigDecimal unitCost;

    // Total = quantity * unitCost
    @Column(name = "total_cost",
            precision = 10, scale = 2)
    private BigDecimal totalCost;

    // How many units actually received so far
    // Supports partial receipts
    @Column(name = "received_qty")
    @Builder.Default
    private Integer receivedQty = 0;
}