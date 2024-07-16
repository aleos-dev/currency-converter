package com.aleos.servlet.common;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
    TRACE;

    public boolean isMatches(String method) {
        return this.name().equalsIgnoreCase(method);
    }
}
