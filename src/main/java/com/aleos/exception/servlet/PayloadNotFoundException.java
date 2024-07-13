package com.aleos.exception.servlet;

public class PayloadNotFoundException extends RuntimeException {

    public PayloadNotFoundException(String message) {
        super(message);
    }
}
