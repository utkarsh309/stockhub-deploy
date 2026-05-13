package com.stockhub.purchase.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PORequest {

    // Supplier to order from
    @NotNull(message = "Supplier is required")
    private Integer supplierId;

    // Warehouse to deliver to
    @NotNull(message = "Warehouse is required")
    private Integer warehouseId;

    // User creating this PO
    @NotNull(message = "Created by is required")
    private Integer createdBy;

    // Optional expected delivery date
    private LocalDateTime expectedDate;

    // Optional notes
    private String notes;

    // List of products to order
    @NotNull(message = "Line items are required")
    private List<LineItemRequest> lineItems;
}