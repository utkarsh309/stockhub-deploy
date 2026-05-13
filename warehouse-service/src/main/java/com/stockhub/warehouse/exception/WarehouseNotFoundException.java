package com.stockhub.warehouse.exception;

public class WarehouseNotFoundException
        extends RuntimeException {

    public WarehouseNotFoundException(String message) {
        super(message);
    }
}