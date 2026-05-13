package com.stockhub.movement.enums;

public enum MovementType {
    STOCK_IN,       // Goods received from supplier
    STOCK_OUT,      // Stock issued or consumed
    TRANSFER_IN,    // Received from another warehouse
    TRANSFER_OUT,   // Sent to another warehouse
    ADJUSTMENT,     // Manual stock correction
    WRITE_OFF       // Damaged or expired removal
}