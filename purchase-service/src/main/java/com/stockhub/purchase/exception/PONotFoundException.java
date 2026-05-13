package com.stockhub.purchase.exception;

public class PONotFoundException
        extends RuntimeException {
    public PONotFoundException(String message) {
        super(message);
    }
}