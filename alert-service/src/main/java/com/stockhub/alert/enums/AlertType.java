package com.stockhub.alert.enums;

public enum AlertType {
    LOW_STOCK,      // Stock below reorder level
    OVERSTOCK,      // Stock above max level
    PO_PENDING,     // PO waiting for approval
    OVERDUE_PO,     // PO delivery date passed
    SYSTEM          // General system alert
}