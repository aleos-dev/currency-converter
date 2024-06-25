package com.aleos.exceptions.daos;

import java.sql.SQLException;

public class ParseResultSetException extends DaoOperationException {

    public ParseResultSetException(String message, SQLException e) {
        super(message, e);
    }
}
