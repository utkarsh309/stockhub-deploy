package com.stockhub.supplier.exception;

public class SupplierNotFoundException
        extends RuntimeException {

    public SupplierNotFoundException(String message) {
        super(message);
    }
}