package com.aleos.daos;

import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.models.entities.ConversionRate;
import com.aleos.models.entities.Currency;
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
                cr.base_currency_id AS cr_base_currency_id,
                cr.target_currency_id AS cr_target_currency_id,
                cr.rate AS cr_rate,
                base_cur.fullname AS base_cur_fullname,
                base_cur.code AS base_cur_code,
                base_cur.sign AS base_cur_sign,
                target_cur.fullname AS target_cur_fullname,
                target_cur.code AS target_cur_code,
                target_cur.sign AS target_cur_sign
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

    private static final String SELECT_CROSS_RATE_BY_CODE = """
            WITH
            indirect_rates AS (
            -- c -> a & c -> b
                 SELECT
                        from_currency.code AS base_cur_code,
                        target_currency.code AS target_cur_code,
                        (1.0 / cr1.rate * cr2.rate) AS cr_rate
                    FROM conversion_rates AS cr1
                    JOIN conversion_rates AS cr2 ON cr1.base_currency_id = cr2.base_currency_id
                    JOIN currencies AS from_currency ON cr1.target_currency_id = from_currency.id
                    JOIN currencies AS target_currency ON target_currency.id = cr2.target_currency_id
                    WHERE (from_currency.code = ? AND target_currency.code = ?)
            
                 UNION
            
            -- a -> c & c -> b
                SELECT
                    from_currency.code AS base_cur_code,
                    target_currency.code AS target_cur_code,
                    (cr1.rate * cr2.rate) AS cr_rate
                FROM conversion_rates AS cr1
                JOIN conversion_rates AS cr2 ON cr1.target_currency_id = cr2.base_currency_id
                JOIN currencies AS from_currency ON cr1.base_currency_id = from_currency.id
                JOIN currencies AS target_currency ON cr2.target_currency_id = target_currency.id
                WHERE (from_currency.code = ? AND target_currency.code = ?)
            
                UNION
            
            -- a -> c & b -> c
                SELECT
                    from_currency.code AS base_cur_code,
                    target_currency.code AS target_cur_code,
                    (cr1.rate * cr2.rate) AS cr_rate
                FROM conversion_rates AS cr1
                JOIN conversion_rates AS cr2 ON cr1.target_currency_id = cr2.target_currency_id
                JOIN currencies AS from_currency ON cr1.base_currency_id = from_currency.id
                JOIN currencies AS target_currency ON   target_currency.id = cr2.base_currency_id
                WHERE (from_currency.code = ? AND target_currency.code = ?)
            
                UNION
            
            -- c -> a & b -> c
                SELECT
                    from_currency.code AS base_cur_code,
                    target_currency.code AS target_cur_code,
                    (cr1.rate * cr2.rate) AS cr_rate
                FROM conversion_rates AS cr1
                JOIN conversion_rates AS cr2 ON cr1.base_currency_id = cr2.target_currency_id
                JOIN currencies AS from_currency ON cr1.target_currency_id = from_currency.id
                JOIN currencies AS target_currency ON   target_currency.id = cr2.base_currency_id
                WHERE (from_currency.code = ? AND target_currency.code = ?)
            )
            SELECT
                0 AS cr_id,
                from_currency.id AS cr_base_currency_id,
                target_currency.id AS cr_target_currency_id,
                cr.cr_rate AS cr_rate,
                from_currency.fullname AS base_cur_fullname,
                from_currency.code AS base_cur_code,
                from_currency.sign AS base_cur_sign,
                target_currency.fullname AS target_cur_fullname,
                target_currency.code AS target_cur_code,
                target_currency.sign AS target_cur_sign
            FROM indirect_rates AS cr
            JOIN currencies AS from_currency ON cr.base_cur_code = from_currency.code
            JOIN currencies AS target_currency ON cr.target_cur_code = target_currency.code
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
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                throw new DaoOperationException("Unknown database error, rollback is failed", e);
            }
            throw new DaoOperationException("Transaction failed and was rolled back.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Unknown database error, cannot close connection.");
                }
            }
        }
    }

    public Optional<ConversionRate> find(@NonNull String from, @NonNull String to) {
        try (var connection = dataSource.getConnection();
             var statement = createFindStatement(from, to, connection)
        ) {
            var resultSet = statement.executeQuery();
            return mapSingleResult(resultSet);
        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    public boolean update(@NonNull String from, @NonNull String to, @NonNull BigDecimal rate) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(UPDATE_RATE_BY_CURRENCY_CODES_SQL)
        ) {

            populateUpdateStatement(statement, from, to, rate);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    public Optional<ConversionRate> findCrossRate(@NonNull String from, @NonNull String to) {
        try (var connection = dataSource.getConnection();
             var statement = createFindCrossRateStatement(from, to, connection)) {

            var resultSet = statement.executeQuery();
            return mapSingleResult(resultSet);

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    @Override
    protected PreparedStatement createSaveStatement(ConversionRate newConversionRate, Connection connection)
            throws SQLException {
        var statement = connection.prepareStatement(INSERT_SQL, RETURN_GENERATED_KEYS);
        populateStatementWithParameters(statement, newConversionRate);

        return statement;
    }

    @Override
    protected PreparedStatement createFindStatement(Integer id, Connection connection) throws SQLException {
        var statement = connection.prepareStatement(SELECT_BY_ID_SQL, RETURN_GENERATED_KEYS);
        statement.setInt(1, id);

        return statement;
    }

    @Override
    protected PreparedStatement createUpdateStatement(ConversionRate conversionRate, Connection connection)
            throws SQLException {
        var statement = connection.prepareStatement(UPDATE_BY_ID_SQL);
        populateStatementWithParameters(statement, conversionRate);
        statement.setInt(4, conversionRate.getId());

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
        var baseCurrency = new Currency();
        baseCurrency.setId(rs.getInt("cr_base_currency_id"));
        baseCurrency.setFullname(rs.getString("base_cur_fullname"));
        baseCurrency.setCode(rs.getString("base_cur_code"));
        baseCurrency.setSign(rs.getString("base_cur_sign"));

        var targetCurrency = new Currency();
        targetCurrency.setId(rs.getInt("cr_target_currency_id"));
        targetCurrency.setFullname(rs.getString("target_cur_fullname"));
        targetCurrency.setCode(rs.getString("target_cur_code"));
        targetCurrency.setSign(rs.getString("target_cur_sign"));

        var conversionRate = new ConversionRate();
        conversionRate.setId(rs.getInt("cr_id"));
        conversionRate.setBaseCurrency(baseCurrency);
        conversionRate.setTargetCurrency(targetCurrency);
        conversionRate.setRate(rs.getBigDecimal("cr_rate"));

        return conversionRate;
    }

    @Override
    protected void populateStatementWithParameters(
            PreparedStatement statement, ConversionRate conversionRate) throws SQLException {
        statement.setInt(1, conversionRate.getBaseCurrency().getId());
        statement.setInt(2, conversionRate.getTargetCurrency().getId());
        statement.setBigDecimal(3, conversionRate.getRate());
    }


    protected PreparedStatement createFindCrossRateStatement(String from,
                                                             String to,
                                                             Connection connection) throws SQLException {

        var statement = connection.prepareStatement(SELECT_CROSS_RATE_BY_CODE);

        // a->c & c->b
        statement.setString(1, from);
        statement.setString(2, to);

        // c->a & c->b
        statement.setString(1, from);
        statement.setString(2, to);

        // a->c & b->c
        statement.setString(1, from);
        statement.setString(2, to);

        // c->a & b->c
        statement.setString(1, from);
        statement.setString(2, to);

        return statement;
    }

    private ConversionRate saveAndFetch(String from,
                                        String to,
                                        BigDecimal rate,
                                        Connection connection) throws SQLException {

        int id = saveByCodes(from, to, rate, connection);

        return find(id, connection)
                .orElseThrow(() -> new DaoOperationException(String.format(
                        "Unexpected database error! The saved with id = %s is not found in the database.", id)));
    }

    private int saveByCodes(String from,
                            String to,
                            BigDecimal rate,
                            Connection connection) throws SQLException {

        try (var statement = connection.prepareStatement(
                INSERT_WITH_CURRENCY_CODES_AS_FOREIGN_KEYS_SQL, RETURN_GENERATED_KEYS)) {

            populateInsertStatement(statement, from, to, rate);

            statement.executeUpdate();
            return fetchGeneratedId(statement);
        }
    }

    private PreparedStatement createFindStatement(String from, String to, Connection connection) throws SQLException {
        var statement = connection.prepareStatement(SELECT_BY_CURRENCY_CODES_SQL);
        statement.setString(1, from);
        statement.setString(2, to);

        return statement;
    }

    private void populateInsertStatement(PreparedStatement statement,
                                         String from,
                                         String to,
                                         BigDecimal rate) throws SQLException {
        statement.setString(1, from);
        statement.setString(2, to);
        statement.setBigDecimal(3, rate);
    }

    private void populateUpdateStatement(PreparedStatement statement,
                                         String from,
                                         String to,
                                         BigDecimal rate) throws SQLException {
        statement.setBigDecimal(1, rate);
        statement.setString(2, from);
        statement.setString(3, to);
    }
}
