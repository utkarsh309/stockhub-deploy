package com.stockhub.supplier.exception;

// Thrown when trying to create PO
// for inactive supplier
public class SupplierInactiveException
        extends RuntimeException {

    public SupplierInactiveException(String message) {
        super(message);
    }
}