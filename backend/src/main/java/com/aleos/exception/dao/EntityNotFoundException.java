package com.aleos.exception.dao;

public class EntityNotFoundException extends DaoOperationException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
