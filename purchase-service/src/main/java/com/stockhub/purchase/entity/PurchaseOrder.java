package com.stockhub.purchase.entity;

import com.stockhub.purchase.enums.PurchaseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    // Primary key auto generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "po_id")
    private Integer poId;

    // Supplier from supplier-service
    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;

    // Warehouse where goods will be delivered
    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;

    // User who created this PO
    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    // Current PO status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PurchaseStatus status =
            PurchaseStatus.DRAFT;

    // Total value of all line items combined
    @Column(name = "total_amount",
            precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Expected delivery date from supplier
    @Column(name = "expected_date")
    private LocalDateTime expectedDate;

    // Actual date goods were received
    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    // Optional notes on this PO
    @Column(name = "notes")
    private String notes;

    // List of products in this PO
    // CascadeType.ALL = line items saved
    // and deleted with parent PO
    // FetchType.EAGER = load items with PO
    @OneToMany(
            mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    private List<POLineItem> lineItems;

    // Auto set when PO created
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Auto set when PO updated
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}