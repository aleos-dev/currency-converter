package com.aleos.dao;

import com.aleos.exception.dao.DaoOperationException;
import com.aleos.exception.dao.UniqueConstraintViolationException;
import com.aleos.model.entity.ConversionRate;
import com.aleos.model.entity.Currency;
import lombok.NonNull;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class ConversionRateDao extends CrudDao<ConversionRate, Integer> {

    private static final Logger logger = Logger.getLogger(ConversionRateDao.class.getName());

    private static final String INSERT_SQL =
            "INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) values (?, ?, ?);";

    private static final String INSERT_WITH_CURRENCY_CODES_AS_FOREIGN_KEYS_SQL = """
            INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate)
            VALUES ((SELECT id FROM currencies WHERE code = ?), (SELECT id FROM currencies WHERE code = ?), ?)
            """;

    private static final String SELECT_ALL_SQL = """
            SELECT
                cr.id AS cr_id,
                cr.base_currency_id AS cr_base_id,
                cr.target_currency_id AS cr_target_id,
                cr.rate AS cr_rate,
                base_cur.fullname AS base_fullname,
                base_cur.code AS base_code,
                base_cur.sign AS base_sign,
                target_cur.fullname AS target_fullname,
                target_cur.code AS target_code,
                target_cur.sign AS target_sign
            FROM conversion_rates cr
            JOIN currencies AS base_cur ON cr.base_currency_id = base_cur.id
            JOIN currencies AS target_cur ON cr.target_currency_id = target_cur.id
            """;

    private static final String SELECT_BY_ID_SQL = String.format("%s %s", SELECT_ALL_SQL, "WHERE cr.id = ?;");

    private static final String SELECT_BY_CURRENCY_CODES_SQL =
            String.format("%s %s", SELECT_ALL_SQL, "WHERE base_cur.code = ? AND target_cur.code = ?;");

    private static final String UPDATE_BY_ID_SQL =
            "UPDATE conversion_rates SET base_currency_id = ?, target_currency_id = ?, rate = ? WHERE id = ?;";

    private static final String UPDATE_RATE_BY_CURRENCY_CODES_SQL = """
            UPDATE conversion_rates
            SET rate = ?
            WHERE base_currency_id = (SELECT id FROM currencies WHERE code = ?)
              AND target_currency_id = (SELECT id FROM currencies WHERE code = ?)
            """;

    private static final String DELETE_BY_ID_SQL = "DELETE FROM conversion_rates WHERE id = ?;";

    private static final String SELECT_CROSS_RATE_BY_CODES = """
            WITH
            indirect_rates AS (
                -- c -> a & c -> b
                SELECT
                    f.code AS base_code,
                    t.code AS target_code,
                    (1.0 / cr1.rate * cr2.rate) AS cr_rate
                FROM conversion_rates AS cr1
                JOIN conversion_rates AS cr2 ON cr1.base_currency_id = cr2.base_currency_id
                JOIN currencies AS f ON cr1.target_currency_id = f.id
                JOIN currencies AS t ON t.id = cr2.target_currency_id
                WHERE (f.code = ? AND t.code = ?)

                UNION

                -- a -> c & c -> b
                SELECT
                    f.code AS base_code,
                    t.code AS target_code,
                    (cr1.rate * cr2.rate) AS cr_rate
                FROM conversion_rates AS cr1
                JOIN conversion_rates AS cr2 ON cr1.target_currency_id = cr2.base_currency_id
                JOIN currencies AS f ON cr1.base_currency_id = f.id
                JOIN currencies AS t ON cr2.target_currency_id = t.id
                WHERE (f.code = ? AND t.code = ?)

                UNION

                -- a -> c & b -> c
                SELECT
                    f.code AS base_code,
                    t.code AS target_code,
                    (cr1.rate * cr2.rate) AS cr_rate
                FROM conversion_rates AS cr1
                JOIN conversion_rates AS cr2 ON cr1.target_currency_id = cr2.target_currency_id
                JOIN currencies AS f ON cr1.base_currency_id = f.id
                JOIN currencies AS t ON t.id = cr2.base_currency_id
                WHERE (f.code = ? AND t.code = ?)

                UNION

                -- c -> a & b -> c
                SELECT
                    f.code AS base_code,
                    t.code AS target_code,
                    (1 / cr1.rate * 1 / cr2.rate) AS cr_rate
                FROM conversion_rates AS cr1
                JOIN conversion_rates AS cr2 ON cr1.base_currency_id = cr2.target_currency_id
                JOIN currencies AS f ON cr1.target_currency_id = f.id
                JOIN currencies AS t ON t.id = cr2.base_currency_id
                WHERE (f.code = ? AND t.code = ?)
            )
            SELECT
                0 AS cr_id,
                from_currency.id AS cr_base_id,
                target_currency.id AS cr_target_id,
                cr.cr_rate AS cr_rate,
                from_currency.fullname AS base_fullname,
                from_currency.code AS base_code,
                from_currency.sign AS base_sign,
                target_currency.fullname AS target_fullname,
                target_currency.code AS target_code,
                target_currency.sign AS target_sign
            FROM indirect_rates AS cr
            JOIN currencies AS from_currency ON cr.base_code = from_currency.code
            JOIN currencies AS target_currency ON cr.target_code = target_currency.code
            ORDER BY cr_rate DESC
            LIMIT 1;
            """;

    public ConversionRateDao(DataSource dataSource) {
        super(dataSource);
    }

    public ConversionRate saveAndFetch(@NonNull String from,
                                       @NonNull String to,
                                       @NonNull BigDecimal rate) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            var conversionRate = saveAndFetch(from, to, rate, connection);
            connection.commit();

            return conversionRate;

        } catch (SQLException e) {
            throw rollbackAndGetException(e, connection);
        } finally {
            closeConnection(connection);
        }
    }

    public Optional<ConversionRate> find(@NonNull String baseCurrencyCode, @NonNull String targetCurrencyCode) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(SELECT_BY_CURRENCY_CODES_SQL)
        ) {

            setPreparedStatementParameters(statement, baseCurrencyCode, targetCurrencyCode);
            var resultSet = statement.executeQuery();

            return mapSingleResult(resultSet);

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    public boolean update(@NonNull String baseCurrencyCode,
                          @NonNull String targetCurrencyCode,
                          @NonNull BigDecimal rate) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(UPDATE_RATE_BY_CURRENCY_CODES_SQL)
        ) {

            setPreparedStatementParameters(statement, rate, baseCurrencyCode, targetCurrencyCode);
            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    public Optional<ConversionRate> findCrossRate(@NonNull String baseCurrencyCode,
                                                  @NonNull String targetCurrencyCode) {
        try (var connection = dataSource.getConnection();
             var statement = createFindCrossRateStatement(baseCurrencyCode, targetCurrencyCode, connection)) {

            var resultSet = statement.executeQuery();
            return mapSingleResult(resultSet);

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    @Override
    protected PreparedStatement createSaveStatement(ConversionRate conversionRate,
                                                    Connection connection) throws SQLException {
        var statement = connection.prepareStatement(INSERT_SQL, RETURN_GENERATED_KEYS);
        setPreparedStatementParameters(statement,
                conversionRate.getBaseCurrency().getId(),
                conversionRate.getTargetCurrency().getId(),
                conversionRate.getRate());

        return statement;
    }

    @Override
    protected PreparedStatement createFindStatement(Integer id, Connection connection) throws SQLException {
        var statement = connection.prepareStatement(SELECT_BY_ID_SQL, RETURN_GENERATED_KEYS);
        setPreparedStatementParameters(statement, id);

        return statement;
    }

    @Override
    protected PreparedStatement createUpdateStatement(ConversionRate conversionRate, Connection connection)
            throws SQLException {
        var statement = connection.prepareStatement(UPDATE_BY_ID_SQL);

        setPreparedStatementParameters(statement,
                conversionRate.getBaseCurrency().getId(),
                conversionRate.getTargetCurrency().getId(),
                conversionRate.getRate(),
                conversionRate.getId());

        return statement;
    }

    @Override
    protected PreparedStatement createSelectAllStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(SELECT_ALL_SQL);
    }

    @Override
    protected PreparedStatement createDeleteStatement(Integer id, Connection connection) throws SQLException {
        var statement = connection.prepareStatement(DELETE_BY_ID_SQL);
        statement.setInt(1, id);

        return statement;
    }

    @Override
    protected ConversionRate mapRowToEntity(ResultSet rs) throws SQLException {
        return new ConversionRate(
                rs.getInt("cr_id"),
                new Currency(
                        rs.getInt("cr_base_id"),
                        rs.getString("base_fullname"),
                        rs.getString("base_code"),
                        rs.getString("base_sign")
                ),
                new Currency(
                        rs.getInt("cr_target_id"),
                        rs.getString("target_fullname"),
                        rs.getString("target_code"),
                        rs.getString("target_sign")
                ),
                rs.getBigDecimal("cr_rate")
        );
    }

    protected PreparedStatement createFindCrossRateStatement(String from,
                                                             String to,
                                                             Connection connection) throws SQLException {

        var statement = connection.prepareStatement(SELECT_CROSS_RATE_BY_CODES);

        setPreparedStatementParameters(statement, from, to, from, to, from, to, from, to);

        return statement;
    }

    private ConversionRate saveAndFetch(String baseCurrencyCode,
                                        String targetCurrencyCode,
                                        BigDecimal rate,
                                        Connection connection) throws SQLException {

        int id = save(baseCurrencyCode, targetCurrencyCode, rate, connection);

        return find(id, connection).orElseThrow(() -> new DaoOperationException(String.format(
                "Unexpected database error! The saved with id = %s is not found in the database.", id)));
    }

    private int save(String baseCurrencyCode,
                     String targetCurrencyCode,
                     BigDecimal rate,
                     Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement(
                INSERT_WITH_CURRENCY_CODES_AS_FOREIGN_KEYS_SQL, RETURN_GENERATED_KEYS)) {

            setPreparedStatementParameters(statement, baseCurrencyCode, targetCurrencyCode, rate);
            statement.executeUpdate();

            return fetchGeneratedId(statement);
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Unknown database error, cannot close connection.");
            }
        }
    }

    private DaoOperationException rollbackAndGetException(SQLException originalEx, Connection connection) {
        DaoOperationException exceptionToReturn = isUniqueConstraintException(originalEx)
                ? new UniqueConstraintViolationException("Not saved due to a unique constraint violation.", originalEx)
                : new DaoOperationException("Transaction failed and was rolled back.", originalEx);
        try {
            if (connection != null) {
                connection.rollback();
            }
            return exceptionToReturn;

        } catch (SQLException rollbackEx) {
            rollbackEx.addSuppressed(exceptionToReturn);
            throw new DaoOperationException("Unknown database error, rollback failed", rollbackEx);
        }
    }
}
