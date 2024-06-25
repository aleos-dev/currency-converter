package com.aleos.util;

import com.aleos.exceptions.daos.ParseResultSetException;
import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.exceptions.daos.UniqueConstraintViolationException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class SQLiteExceptionResolver {

    private static final Map<String, Function<SQLException, DaoOperationException>> registerMap = new HashMap<>();

    static {
        registerMap.put("SQLITE_CONSTRAINT_PRIMARYKEY",
                e -> new UniqueConstraintViolationException("Primary key constraint violation", e));

        registerMap.put("SQLITE_CONSTRAINT_UNIQUE",
                e -> new UniqueConstraintViolationException("Unique constraint violation", e));

        registerMap.put("SQLITE_CONSTRAINT_FOREIGNKEY",
                e -> new UniqueConstraintViolationException("Foreign key constraint violation", e));

        registerMap.put("SQLITE_CONSTRAINT_CHECK",
                e -> new UniqueConstraintViolationException("Check constraint violation", e));

        registerMap.put("SQLITE_CONSTRAINT_NOTNULL",
                e -> new UniqueConstraintViolationException("Not null constraint violation", e));

        registerMap.put("S1009", e -> new ParseResultSetException("Invalid column index", e));

        registerMap.put("S0022", e -> new ParseResultSetException("Invalid column name", e));
    }

    private SQLiteExceptionResolver() {}

    public static DaoOperationException wrapException(SQLException e, String message) {

        return getRegisteredException(e)
                .orElse(new DaoOperationException(message, e));
    }

    private static Optional<DaoOperationException> getRegisteredException(SQLException e) {

        var registeredException = registerMap.get(e.getSQLState()).apply(e);
        return Optional.ofNullable(registeredException);
    }
}
