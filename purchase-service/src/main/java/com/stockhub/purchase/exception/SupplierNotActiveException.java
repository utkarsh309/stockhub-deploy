package com.stockhub.purchase.exception;

// Thrown when creating PO for inactive supplier
public class SupplierNotActiveException
        extends RuntimeException {
    public SupplierNotActiveException(String message) {
        super(message);
    }
}