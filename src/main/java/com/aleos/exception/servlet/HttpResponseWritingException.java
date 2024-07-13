package com.aleos.exception.servlet;

public class HttpResponseWritingException extends RuntimeException {

    public HttpResponseWritingException(String message, Throwable cause) {
        super(message, cause);
    }
}
