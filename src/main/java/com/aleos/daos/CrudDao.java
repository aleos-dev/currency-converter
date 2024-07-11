package com.aleos.daos;

import com.aleos.exceptions.UnknownParameterTypeException;
import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.exceptions.daos.UniqueConstraintViolationException;
import com.aleos.models.entities.Entity;
import lombok.NonNull;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class CrudDao<E extends Entity<K>, K> {

    protected final DataSource dataSource;

    protected CrudDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(@NonNull E entity) {
        try (var connection = dataSource.getConnection()) {

            save(entity, connection);

        } catch (SQLException e) {
            if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                throw new UniqueConstraintViolationException("Can't be saved due to a unique constraint violation.", e);
            }
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    public List<E> findAll() {
        try (var connection = dataSource.getConnection();
             var statement = createSelectAllStatement(connection)
        ) {

            var resultSet = statement.executeQuery();
            return mapToList(resultSet);

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    public Optional<E> find(@NonNull K id) {
        try (var connection = dataSource.getConnection()) {

            return find(id, connection);

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    public boolean update(@NonNull E entity) {
        try (var connection = dataSource.getConnection();
             var statement = createUpdateStatement(entity, connection)
        ) {

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    public boolean delete(@NonNull K id) {
        try (var connection = dataSource.getConnection();
             var statement = createDeleteStatement(id, connection)
        ) {

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    protected void save(E entity, Connection connection) throws SQLException {
        try (var statement = createSaveStatement(entity, connection)) {

            statement.executeUpdate();
            K id = fetchGeneratedId(statement);
            entity.setId(id);
        }
    }

    protected Optional<E> find(K id, Connection connection) throws SQLException {
        try (var statement = createFindStatement(id, connection)) {

            ResultSet resultSet = statement.executeQuery();
            return mapSingleResult(resultSet);
        }
    }

    protected List<E> mapToList(ResultSet resultSet) throws SQLException {
        List<E> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(mapRowToEntity(resultSet));
        }

        return list;
    }

    protected K fetchGeneratedId(PreparedStatement statement) throws SQLException {
        ResultSet generatedKeysSet = statement.getGeneratedKeys();
        if (generatedKeysSet.next()) {

            //noinspection unchecked
            return (K) generatedKeysSet.getObject(1);

        } else throw new DaoOperationException(
                String.format("Cannot obtain generatedKey for %s", this.getClass().getSimpleName()));
    }

    protected Optional<E> mapSingleResult(ResultSet resultSet) throws SQLException {
        return resultSet.next()
                ? Optional.of(mapRowToEntity(resultSet))
                : Optional.empty();
    }

    protected void setPreparedStatementParameters(PreparedStatement statement,
                                                  Object... parameters) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            Object param = parameters[i - 1];
            if (param instanceof String str) {
                statement.setString(i, str);
            } else if (param instanceof BigDecimal bd) {
                statement.setBigDecimal(i, bd);
            } else if (param instanceof Integer num) {
                statement.setInt(i, num);
            } else {
                throw new UnknownParameterTypeException("Need add handler for new parameter type: " + param.getClass());
            }
        }
    }

    protected abstract PreparedStatement createSaveStatement(E entity, Connection connection) throws SQLException;

    protected abstract PreparedStatement createSelectAllStatement(Connection connection) throws SQLException;

    protected abstract PreparedStatement createFindStatement(K id, Connection connection) throws SQLException;

    protected abstract PreparedStatement createUpdateStatement(E entity, Connection connection) throws SQLException;

    protected abstract PreparedStatement createDeleteStatement(K id, Connection connection) throws SQLException;

    protected abstract E mapRowToEntity(ResultSet resultSet) throws SQLException;
}
