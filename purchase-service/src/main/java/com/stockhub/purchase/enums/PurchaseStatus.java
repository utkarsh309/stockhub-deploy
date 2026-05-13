package com.stockhub.purchase.enums;

public enum PurchaseStatus {
    DRAFT,      // PO created but not submitted
    PENDING,    // Submitted for approval
    APPROVED,   // Approved by manager
    RECEIVED,   // Goods received fully
    CANCELLED   // PO cancelled
}