package com.aleos.exception.servlet;

public class BadParameterException extends RuntimeException {
    public BadParameterException(String message) {
        super(message);
    }
}
