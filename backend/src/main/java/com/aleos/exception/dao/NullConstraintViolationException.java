package com.aleos.exception.dao;

import java.sql.SQLException;

public class NullConstraintViolationException extends DaoOperationException {

    public NullConstraintViolationException(String message, SQLException originalEx) {
        super(message, originalEx);
    }
}
