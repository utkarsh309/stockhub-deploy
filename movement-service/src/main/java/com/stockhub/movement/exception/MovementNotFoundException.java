package com.stockhub.movement.exception;

public class MovementNotFoundException
        extends RuntimeException {
    public MovementNotFoundException(String message) {
        super(message);
    }
}