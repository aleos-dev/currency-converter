package com.aleos.exceptions.servlets;

public class PayloadNotFoundException extends RuntimeException {

    public PayloadNotFoundException(String message) {
        super(message);
    }
}
