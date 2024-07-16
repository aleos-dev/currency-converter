package com.aleos.exception.dao;

import java.sql.SQLException;

public class UniqueConstraintViolationException extends DaoOperationException {

    public UniqueConstraintViolationException(String message, SQLException e) {
        super(message, e);
    }
}
