package com.stockhub.purchase.exception;

// Thrown when invalid status transition attempted
// Example: approving already cancelled PO
public class InvalidPOStatusException
        extends RuntimeException {
    public InvalidPOStatusException(String message) {
        super(message);
    }
}