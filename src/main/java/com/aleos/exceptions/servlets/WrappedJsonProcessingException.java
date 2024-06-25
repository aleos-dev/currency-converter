package com.aleos.exceptions.servlets;

public class WrappedJsonProcessingException extends RuntimeException {

    public WrappedJsonProcessingException(String message, Exception e) {
        super(message, e);
    }
}
