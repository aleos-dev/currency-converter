package com.aleos.daos;

import com.aleos.models.entities.ConversionRate;
import com.aleos.models.entities.Currency;
import com.aleos.util.SQLiteExceptionResolver;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Objects;
import java.util.Optional;

import static java.sql.Statement.*;

public class ConversionRateDao extends CrudDao<ConversionRate, Integer> {

    private static final String INSERT_BY_ID_SQL =
            "INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) values (?, ?, ?);";

    private static final String INSERT_BY_CURRENCY_CODES_SQL =
            """
                    INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate)
                    VALUES ((SELECT id FROM currencies WHERE code = ?), (SELECT id FROM currencies WHERE code = ?), ?)
                    """;

    private static final String SELECT_ALL_SQL = """
            SELECT
                cr.id as cr_id,
                cr.base_currency_id as cr_base_currency_id,
                cr.target_currency_id as cr_target_currency_id,
                cr.rate as cr_rate,
                base_cur.fullname as base_cur_fullname,
                base_cur.code as base_cur_code,
                base_cur.sign as base_cur_sign,
                target_cur.fullname as target_cur_fullname,
                target_cur.code as target_cur_code,
                target_cur.sign as target_cur_sign
            FROM conversion_rates cr
            JOIN currencies AS base_cur ON cr.base_currency_id = base_cur.id
            JOIN currencies AS target_cur ON cr.target_currency_id = target_cur.id
            """;

    private static final String SELECT_BY_ID_SQL = String.format("%s %s", SELECT_ALL_SQL, "WHERE cr.id = ?;");

    private static final String SELECT_BY_CODE_SQL =
            String.format("%s %s", SELECT_ALL_SQL, "WHERE base_cur.code = ? AND target_cur.code = ?;");

    private static final String UPDATE_BY_ID_SQL =
            "UPDATE conversion_rates SET base_currency_id = ?, target_currency_id = ?, rate = ? WHERE id = ?;";

    private static final String UPDATE_RATE_BY_CURRENCY_CODES_SQL =
            """
                    UPDATE conversion_rates cr
                    JOIN currencies bc ON cr.base_currency_id = bc.id
                    JOIN currencies tc ON cr.target_currency_id = tc.id
                    SET cr.rate = ?
                    WHERE bc.code = ? AND tc.code = ?
                    """;

    private static final String DELETE_BY_ID_SQL = "DELETE FROM conversion_rates WHERE id = ?;";

    public ConversionRateDao(DataSource dataSource) {
        super(dataSource);
    }

    protected Integer save(String baseCurrency, String tartgetCurrency, BigDecimal rate) {

        try (var connection = dataSource.getConnection();
             var statement =
                     connection.prepareStatement(INSERT_BY_CURRENCY_CODES_SQL, RETURN_GENERATED_KEYS)) {

            populateInsertStatementByCodes(statement, baseCurrency, tartgetCurrency, rate);

            statement.executeUpdate();

            return fetchGeneratedId(statement);

        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e, String.format(
                    "Error saving conversion rate with code %s%s", baseCurrency, tartgetCurrency));
        }
    }

    public Optional<ConversionRate> findByCode(String code) {

        Objects.requireNonNull(code);

        try (var connection = dataSource.getConnection()) {

            return findEntityByCode(code, connection);

        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e, "Error during findByCode request process");
        }
    }

    public void updateRateByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(UPDATE_RATE_BY_CURRENCY_CODES_SQL)) {

            populateUpdateStatementByCodes(statement, baseCurrencyCode, targetCurrencyCode, rate);

            executeUpdateStatement(String.join("", baseCurrencyCode, targetCurrencyCode), statement);

        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e,
                    "Error updating %s%s conversion rate.".formatted(baseCurrencyCode, targetCurrencyCode));
        }
    }


    @Override
    protected PreparedStatement createSaveStatement(ConversionRate newConversionRate, Connection connection)
            throws SQLException {

        var statement = connection.prepareStatement(INSERT_BY_ID_SQL, RETURN_GENERATED_KEYS);
        populateStatementWithParameters(statement, newConversionRate);

        return statement;
    }

    @Override
    protected PreparedStatement createFindByIdStatement(Integer id, Connection connection) throws SQLException {

        var statement = connection.prepareStatement(SELECT_BY_ID_SQL, RETURN_GENERATED_KEYS);
        statement.setInt(4, id);

        return statement;
    }

    @Override
    protected PreparedStatement createUpdateStatement(ConversionRate conversionRate, Connection connection)
            throws SQLException {

        var statement = connection.prepareStatement(UPDATE_BY_ID_SQL);
        populateStatementWithParameters(statement, conversionRate);

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

    protected PreparedStatement createFindByCodeStatement(String code, Connection connection) throws SQLException {

        var statement =
                connection.prepareStatement(SELECT_BY_CODE_SQL, RETURN_GENERATED_KEYS);
        statement.setString(1, code);

        return statement;
    }

    @Override
    protected ConversionRate mapRowToEntity(ResultSet rs) {

        try {
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

        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e, "Cannot parse row to create ConversionRate instance");
        }
    }

    @Override
    protected void populateStatementWithParameters(
            PreparedStatement statement, ConversionRate conversionRate) throws SQLException {

        statement.setInt(1, conversionRate.getBaseCurrency().getId());
        statement.setInt(2, conversionRate.getTargetCurrency().getId());
        statement.setBigDecimal(3, conversionRate.getRate());
    }

    private static void populateInsertStatementByCodes(PreparedStatement statement,
                                                       String baseCurrencyCode,
                                                       String targetCurrencyCode,
                                                       BigDecimal rate
    ) throws SQLException {

        statement.setString(1, baseCurrencyCode);
        statement.setString(2, targetCurrencyCode);
        statement.setBigDecimal(3, rate);
    }

    private void populateUpdateStatementByCodes(PreparedStatement statement,
                                                String baseCurrencyCode,
                                                String targetCurrencyCode,
                                                BigDecimal rate
    ) throws SQLException {

        statement.setBigDecimal(1, rate);
        statement.setString(2, baseCurrencyCode);
        statement.setString(3, targetCurrencyCode);
    }

    private Optional<ConversionRate> findEntityByCode(String code, Connection connection) throws SQLException {

        try (var statement = createFindByCodeStatement(code, connection)) {

            ResultSet resultSet = statement.executeQuery();

            return mapSingleResult(resultSet);
        }
    }
}
