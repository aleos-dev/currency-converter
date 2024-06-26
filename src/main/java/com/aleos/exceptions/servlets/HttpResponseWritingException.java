package com.aleos.exceptions.servlets;

public class HttpResponseWritingException extends RuntimeException {

    public HttpResponseWritingException(String message, Throwable cause) {
        super(message, cause);
    }
}
