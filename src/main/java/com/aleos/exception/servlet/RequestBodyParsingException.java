package com.aleos.exception.servlet;

public class RequestBodyParsingException extends RuntimeException {

    public RequestBodyParsingException(String message, Exception e) {
        super(message, e);
    }

    public RequestBodyParsingException(String s) {
        super(s);
    }
}
