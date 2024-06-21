package com.aleos.daos;

import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.models.entities.Currency;
import com.aleos.util.SQLiteExceptionResolver;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CurrencyDao extends CrudDao<Currency, Integer> {

    private static final String INSERT_SQL = "INSERT INTO currencies (fullname, code, sign) VALUES (?, ?, ?);";

    private static final String SELECT_ALL_SQL = "SELECT id, fullname, code, sign FROM currencies;";

    private static final String SELECT_BY_ID_SQL = "SELECT id, fullname, code, sign FROM currencies WHERE id = ?";

    private static final String SELECT_BY_CODE_SQL = "SELECT id, fullname, code, sign FROM currencies WHERE code = ?";

    private static final String UPDATE_CURRENCY_SQL =
            "UPDATE currencies SET fullname = ?, code = ? , sign = ? WHERE id = ?;";

    private static final String DELETE_BY_ID_SQL = "DELETE FROM currencies WHERE id = ?";

    public CurrencyDao(DataSource dataSource) {
        super(dataSource);
    }


    public Optional<Currency> findByCode(String code) {

        Objects.requireNonNull(code);

        try (var connection = dataSource.getConnection()) {

            return findEntityByCode(code, connection);
        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e, "Error during findByCode request process");
        }
    }

    @Override
    protected PreparedStatement createSaveStatement(Currency currency, Connection connection) {

        try {
            var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            populateStatementWithCurrencyParameters(statement, currency);

            return statement;

        } catch (SQLException e) {
            throw SQLiteExceptionResolver
                    .wrapException(e, "Error preparing saving statement for Currency: %s".formatted(currency));
        }
    }

    @Override
    protected List<Currency> convertResultSetToList(ResultSet rs) throws SQLException {

        List<Currency> currencyList = new ArrayList<>();
        while (rs.next()) {
            currencyList.add(mapRowToEntity(rs));
        }

        return currencyList;
    }

    @Override
    protected Currency mapRowToEntity(ResultSet rs) {

        try {
            var currency = new Currency();
            currency.setId(rs.getInt("id"));
            currency.setFullname(rs.getString("fullname"));
            currency.setCode(rs.getString("code"));
            currency.setSign(rs.getString("sign"));

            return currency;
        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e, "Cannot parse row to create Currency instance");
        }
    }

    @Override
    protected PreparedStatement createSelectAllStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(SELECT_ALL_SQL);
    }

    @Override
    protected Optional<Currency> findEntityById(Integer id, Connection connection) throws SQLException {

        try (var statement = createPreparedStatementBasedOnUniqueKey(id, connection, SELECT_BY_ID_SQL)) {
            return processStatementAndMapResult(statement);
        }
    }

    @Override
    protected void updateEntity(Currency currency, Connection connection) throws SQLException {

        try ( var statement = connection.prepareStatement(UPDATE_CURRENCY_SQL)) {

            populateStatementWithCurrencyParameters(statement, currency);
            statement.setInt(4, currency.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new DaoOperationException(
                        String.format("Currency with id = %d does not exist", currency.getId()));
            }
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

    private Optional<Currency> findEntityByCode(String code, Connection connection) throws SQLException {

        try (var statement = createPreparedStatementBasedOnUniqueKey(code, connection, SELECT_BY_CODE_SQL)) {
            return processStatementAndMapResult(statement);
        }
    }

    private void populateStatementWithCurrencyParameters(PreparedStatement statement, Currency currency)
            throws SQLException {

        statement.setString(1, currency.getFullname());
        statement.setString(2, currency.getCode());
        statement.setString(3, currency.getSign());
    }

    private Optional<Currency> processStatementAndMapResult(PreparedStatement statement) throws SQLException {

        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            return Optional.of(mapRowToEntity(rs));
        }

        return Optional.empty();
    }

    private PreparedStatement createPreparedStatementBasedOnUniqueKey(
            Object uniqueKey,
            Connection connection,
            String sqlQuery
    ) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(sqlQuery);
        if (uniqueKey instanceof Integer intKey) {
            statement.setInt(1, intKey);
        } else if (uniqueKey instanceof String stringKey) {
            statement.setString(1, stringKey);
        }

        return statement;
    }
}