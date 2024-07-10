package com.aleos.daos;

import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.models.entities.Currency;
import lombok.NonNull;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class CurrencyDao extends CrudDao<Currency, Integer> {

    private static final String INSERT_SQL = "INSERT INTO currencies (fullname, code, sign) VALUES (?, ?, ?);";

    private static final String SELECT_ALL_SQL = "SELECT id, fullname, code, sign FROM currencies;";

    private static final String SELECT_BY_ID_SQL = "SELECT id, fullname, code, sign FROM currencies WHERE id = ?;";

    private static final String SELECT_BY_CODE_SQL =
            "SELECT id, fullname, code, sign FROM currencies WHERE code = ?;";

    private static final String UPDATE_BY_ID_SQL =
            "UPDATE currencies SET fullname = ?, code = ? , sign = ? WHERE id = ?;";

    private static final String DELETE_BY_ID_SQL = "DELETE FROM currencies WHERE id = ?;";

    public CurrencyDao(DataSource dataSource) {
        super(dataSource);
    }

    public Optional<Currency> find(@NonNull String code) {
        try (var connection = dataSource.getConnection()) {

            return find(code, connection);

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    @Override
    protected PreparedStatement createSaveStatement(Currency newCurrency, Connection connection) throws SQLException {
        var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
        populateStatementWithParameters(statement, newCurrency);

        return statement;
    }

    @Override
    protected PreparedStatement createFindStatement(Integer id, Connection connection) throws SQLException {
        var statement = connection.prepareStatement(SELECT_BY_ID_SQL, Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, id);

        return statement;
    }

    @Override
    protected PreparedStatement createUpdateStatement(Currency entity, Connection connection) throws SQLException {
        var statement = connection.prepareStatement(UPDATE_BY_ID_SQL);
        populateStatementWithParameters(statement, entity);
        statement.setInt(4, entity.getId());

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

    protected PreparedStatement createFindStatement(String code, Connection connection) throws SQLException {
        var statement =
                connection.prepareStatement(SELECT_BY_CODE_SQL, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, code);

        return statement;
    }

    @Override
    protected Currency mapRowToEntity(ResultSet rs) throws SQLException {
        var currency = new Currency();
        currency.setId(rs.getInt("id"));
        currency.setFullname(rs.getString("fullname"));
        currency.setCode(rs.getString("code"));
        currency.setSign(rs.getString("sign"));

        return currency;
    }

    @Override
    protected void populateStatementWithParameters(PreparedStatement statement, Currency entity)
            throws SQLException {
        statement.setString(1, entity.getFullname());
        statement.setString(2, entity.getCode());
        statement.setString(3, entity.getSign());
    }

    private Optional<Currency> find(String code, Connection connection) throws SQLException {
        try (var statement = createFindStatement(code, connection)) {

            ResultSet resultSet = statement.executeQuery();
            return mapSingleResult(resultSet);
        }
    }
}