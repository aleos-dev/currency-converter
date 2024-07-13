package com.aleos.exception.dao;

import java.sql.SQLException;

public class DaoOperationException extends RuntimeException {

    public DaoOperationException(String message, SQLException e) {
        super(message, e);
    }

    public DaoOperationException(String message) {
        super(message);
    }
}
