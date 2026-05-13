package com.stockhub.movement.entity;

import com.stockhub.movement.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    // Primary key auto generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Integer movementId;

    // Product that was moved
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    // Warehouse where movement happened
    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;

    // Type of movement
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    // How many units moved
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Stock balance after this movement
    // Used for historical reconstruction
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    // ID of related document (PO ID, etc)
    @Column(name = "reference_id")
    private Integer referenceId;

    // Type of reference (PO, TRANSFER, etc)
    @Column(name = "reference_type")
    private String referenceType;

    // User who performed this movement
    @Column(name = "performed_by", nullable = false)
    private Integer performedBy;

    // Optional notes on movement
    @Column(name = "notes")
    private String notes;

    // When movement happened
    @Column(name = "movement_date")
    @Builder.Default
    private LocalDateTime movementDate =
            LocalDateTime.now();
}