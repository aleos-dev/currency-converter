package com.aleos.exception.servlet;

public class WrappedJsonProcessingException extends RuntimeException {

    public WrappedJsonProcessingException(String message, Exception e) {
        super(message, e);
    }
}
