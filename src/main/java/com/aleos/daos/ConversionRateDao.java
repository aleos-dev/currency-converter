package com.aleos.daos;

import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.models.entities.ConversionRate;
import com.aleos.models.entities.Currency;
import com.aleos.util.SQLiteExceptionResolver;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ConversionRateDao extends CrudDao<ConversionRate, Integer> {

    private static final String INSERT_SQL =
            "INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) values (?, ?, ?);";

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

    private static final String UPDATE_CONVERSION_RATE_SQL =
            "UPDATE conversion_rates SET base_currency_id = ?, target_currency_id = ?, rate = ? WHERE id = ?;";
    private static final String DELETE_BY_ID_SQL = "DELETE FROM conversion_rates WHERE id = ?;";

    public ConversionRateDao(DataSource dataSource) {
        super(dataSource);
    }

    public Optional<ConversionRate> findByCode(String code) {

        Objects.requireNonNull(code);

        try (var connection = dataSource.getConnection()) {

            return findEntityByCode(code, connection);

        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e, "Error during findByCode request process");
        }
    }

    @Override
    protected PreparedStatement createSaveStatement(ConversionRate conversionRate, Connection connection) {

        try {
            var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            populateStatementWithConversionRateParameters(statement, conversionRate);
            return statement;

        } catch (SQLException e) {

            throw new DaoOperationException("Error preparing saving statement for Currency: %s".formatted(conversionRate), e);
        }
    }

    @Override
    protected List<ConversionRate> convertResultSetToList(ResultSet rs) throws SQLException {

        List<ConversionRate> conversionRateList = new ArrayList<>();
        while (rs.next()) {
            conversionRateList.add(mapRowToEntity(rs));
        }

        return conversionRateList;
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
    protected PreparedStatement createSelectAllStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(SELECT_ALL_SQL);
    }

    @Override
    protected Optional<ConversionRate> findEntityById(Integer id, Connection connection) throws SQLException {

        try (var statement = createPreparedStatementBasedOnUniqueKey(id, connection, SELECT_BY_ID_SQL)) {
            return processStatementAndMapResult(statement);
        }
    }

    @Override
    protected void updateEntity(ConversionRate conversionRate, Connection connection) throws SQLException {

        try ( var statement = connection.prepareStatement(UPDATE_CONVERSION_RATE_SQL)) {
            populateStatementWithConversionRateParameters(statement, conversionRate);
            statement.setInt(4, conversionRate.getId());

            executeUpdateStatement(conversionRate.getId(), statement);
        }
    }

    @Override
    protected void deleteById(Integer id, Connection connection) {

        try (var statement = connection.prepareStatement(DELETE_BY_ID_SQL)) {
            statement.setInt(1, id);

            executeUpdateStatement(id, statement);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error removing product by id = %d", id), e);
        }
    }

    private Optional<ConversionRate> findEntityByCode(String code, Connection connection) throws SQLException {

        try (var statement = createPreparedStatementBasedOnUniqueKey(code, connection, SELECT_BY_CODE_SQL)) {
            statement.setString(1, code.substring(0, 3));
            statement.setString(2, code.substring(3));

            return processStatementAndMapResult(statement);
        }
    }

    private Optional<ConversionRate> processStatementAndMapResult(PreparedStatement statement) throws SQLException {

        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            return Optional.of(mapRowToEntity(rs));
        }
        return Optional.empty();
    }

    private void populateStatementWithConversionRateParameters(
            PreparedStatement statement, ConversionRate conversionRate) throws SQLException {

        statement.setInt(1, conversionRate.getBaseCurrency().getId());
        statement.setInt(2, conversionRate.getTargetCurrency().getId());
        statement.setBigDecimal(3, conversionRate.getRate());
    }

    private PreparedStatement createPreparedStatementBasedOnUniqueKey(
            Object uniqueKey,
            Connection connection,
            String sqlQuery
    ) throws SQLException {

        var statement = connection.prepareStatement(sqlQuery);
        if (uniqueKey instanceof Integer intId) {
            statement.setInt(1, intId);
        } else if (uniqueKey instanceof String stringId) {
            statement.setString(1, stringId);
        }

        return statement;
    }
}
