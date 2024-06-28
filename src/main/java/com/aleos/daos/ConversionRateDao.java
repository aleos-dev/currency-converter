package com.aleos.daos;

import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.models.entities.ConversionRate;
import com.aleos.models.entities.Currency;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.util.Objects.nonNull;
import static java.util.logging.Level.SEVERE;

public class ConversionRateDao extends CrudDao<ConversionRate, Integer> {

    private static final Logger LOGGER = Logger.getLogger(ConversionRateDao.class.getName());

    private static final String INSERT_SQL =
            "INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) values (?, ?, ?);";

    private static final String INSERT_WITH_CURRENCY_CODES_AS_FOREIGN_KEYS_SQL = """
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

    private static final String SELECT_BY_CURRENCY_CODES_SQL =
            String.format("%s %s", SELECT_ALL_SQL, "WHERE base_cur.code = ? AND target_cur.code = ?;");

    private static final String UPDATE_BY_ID_SQL =
            "UPDATE conversion_rates SET base_currency_id = ?, target_currency_id = ?, rate = ? WHERE id = ?;";

    private static final String UPDATE_RATE_BY_CURRENCY_CODES_SQL = """
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

    public ConversionRate saveAndReturn(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            var conversionRate = saveAndRetrieveConversionRate(baseCurrencyCode, targetCurrencyCode, rate, connection);
            connection.commit();

            return conversionRate;

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        } finally {
            if (nonNull(connection)) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(SEVERE, "Unexpected error during setAutoCommit flag to true.");
                }
            }
        }
    }

    public Optional<ConversionRate> findByCode(String code) {

        Objects.requireNonNull(code);

        try (var connection = dataSource.getConnection()) {

            return findEntityByCode(code, connection);

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    public void updateRateByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(UPDATE_RATE_BY_CURRENCY_CODES_SQL)) {

            populateUpdateStatementByCodes(statement, baseCurrencyCode, targetCurrencyCode, rate);

            executeUpdateStatement(String.join("", baseCurrencyCode, targetCurrencyCode), statement);

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
    protected PreparedStatement createFindByIdStatement(Integer id, Connection connection) throws SQLException {

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

    protected PreparedStatement createFindByCodeStatement(String code, Connection connection) throws SQLException {

        var statement =
                connection.prepareStatement(SELECT_BY_CURRENCY_CODES_SQL, RETURN_GENERATED_KEYS);
        statement.setString(1, code.substring(0, 3));
        statement.setString(2, code.substring(3, 6));

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

    private ConversionRate saveAndRetrieveConversionRate(String baseCurrencyCode,
                                                         String targetCurrencyCode,
                                                         BigDecimal rate,
                                                         Connection connection) throws SQLException {
        try {
            int conversionRateId = insertConversionRate(baseCurrencyCode, targetCurrencyCode, rate, connection);

            Optional<ConversionRate> result = findEntityById(conversionRateId, connection);

            return result.orElseThrow(() -> new DaoOperationException(
                    String.format("Unexpected result under saveAndReturnInstance method operation. " +
                                  "Saved instance with id = %s is not found in the database.", conversionRateId)));

        } catch (SQLException | DaoOperationException e) {
            connection.rollback();
            throw e;
        }
    }

    private int insertConversionRate(String baseCurrencyCode,
                                     String targetCurrencyCode,
                                     BigDecimal rate,
                                     Connection connection) throws SQLException {

        try (var statement =
                     connection.prepareStatement(INSERT_WITH_CURRENCY_CODES_AS_FOREIGN_KEYS_SQL, RETURN_GENERATED_KEYS)) {

            populateInsertStatementByCodes(statement, baseCurrencyCode, targetCurrencyCode, rate);

            statement.executeUpdate();

            return fetchGeneratedId(statement);
        }
    }
}
