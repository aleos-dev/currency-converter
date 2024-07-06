package com.aleos.exceptions.daos;

public class EntityNotFoundException extends DaoOperationException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
