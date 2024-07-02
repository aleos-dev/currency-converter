package com.aleos.exceptions.servlets;

public class RequestBodyParsingException extends RuntimeException {

    public RequestBodyParsingException(String message, Exception e) {
        super(message, e);
    }

    public RequestBodyParsingException(String s) {
        super(s);
    }
}
