package com.aleos.exceptions.daos;

import java.sql.SQLException;

public class EntityNotFoundException extends DaoOperationException {

    public EntityNotFoundException(String message, SQLException e) {
        super(message, e);
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
