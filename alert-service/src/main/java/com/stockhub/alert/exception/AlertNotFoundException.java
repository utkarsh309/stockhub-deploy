package com.stockhub.alert.exception;

public class AlertNotFoundException
        extends RuntimeException {
    public AlertNotFoundException(String message) {
        super(message);
    }
}