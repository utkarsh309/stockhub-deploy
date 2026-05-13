package com.stockhub.product.exception;

public class DuplicateSkuException
        extends RuntimeException {

    public DuplicateSkuException(String message) {
        super(message);
    }
}