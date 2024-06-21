package com.aleos.util;

import com.aleos.exceptions.daos.ParseResultSetException;
import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.exceptions.daos.UniqueConstraintViolationException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SQLiteExceptionResolver {

    private static final Map<String, DaoOperationException> registerMap = new HashMap<>();

    static {
        registerMap.put("SQLITE_CONSTRAINT_PRIMARYKEY",
                new UniqueConstraintViolationException("Primary key constraint violation"));

        registerMap.put("SQLITE_CONSTRAINT_UNIQUE",
                new UniqueConstraintViolationException("Unique constraint violation"));

        registerMap.put("SQLITE_CONSTRAINT_FOREIGNKEY",
                new UniqueConstraintViolationException("Foreign key constraint violation"));

        registerMap.put("SQLITE_CONSTRAINT_CHECK",
                new UniqueConstraintViolationException("Check constraint violation"));

        registerMap.put("SQLITE_CONSTRAINT_NOTNULL",
                new UniqueConstraintViolationException("Not null constraint violation"));

        registerMap.put("S1009", new ParseResultSetException("Invalid column index"));

        registerMap.put("S0022", new ParseResultSetException("Invalid column name"));
    }

    private SQLiteExceptionResolver() {}

    public static Optional<DaoOperationException> getRegisteredException(SQLException e) {
        return Optional.ofNullable(registerMap.get(e.getSQLState()));
    }

    public static DaoOperationException wrapException(SQLException e, String message) {
        return getRegisteredException(e)
                .orElse(new DaoOperationException(message, e));
    }
}
