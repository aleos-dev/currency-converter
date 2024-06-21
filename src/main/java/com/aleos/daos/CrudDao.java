package com.aleos.daos;

import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.models.entities.Entity;
import com.aleos.util.SQLiteExceptionResolver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class CrudDao<E extends Entity<K>, K> {

    protected final DataSource dataSource;

    protected CrudDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public K save(E entity) {

        Objects.requireNonNull(entity);

        try (var connection = dataSource.getConnection()) {

            saveEntity(entity, connection);
            return entity.getId();

        } catch (SQLException e) {
            throw SQLiteExceptionResolver
                    .wrapException(e, String.format("Error saving %s: %s", entity.getClass().getSimpleName(), entity));
        }
    }

    protected void saveEntity(E entity, Connection connection) throws SQLException {

        try (PreparedStatement statement = createSaveStatement(entity, connection)) {
            statement.executeUpdate();
            K id = fetchGeneratedId(statement);
            entity.setId(id);
        }
    }

    protected K fetchGeneratedId(PreparedStatement statement) throws SQLException {

        ResultSet generatedKeysSet = statement.getGeneratedKeys();

        if (generatedKeysSet.next()) {
            //noinspection unchecked
            return (K) generatedKeysSet.getObject(1);

        } else throw new DaoOperationException(
                String.format("Cannot obtain generatedKey for %s", this.getClass().getSimpleName()));
    }

    protected abstract PreparedStatement createSaveStatement(E entity, Connection connection);

    public List<E> findAll() {

        try (var connection = dataSource.getConnection()) {

            return findAllEntities(connection);
        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e,
                    String.format("Error during findAll of %s", this.getClass().getSimpleName()));
        }
    }

    private List<E> findAllEntities(Connection connection) throws SQLException {

        try (PreparedStatement statement = createSelectAllStatement(connection)) {
            var resultSet = statement.executeQuery();
            return convertResultSetToList(resultSet);
        }
    }

    protected abstract List<E> convertResultSetToList(ResultSet resultSet) throws SQLException;

    protected abstract E mapRowToEntity(ResultSet resultSet);

    protected abstract PreparedStatement createSelectAllStatement(Connection connection) throws SQLException;

    public Optional<E> findById(K id) {

        Objects.requireNonNull(id);

        try (var connection = dataSource.getConnection()) {

            return findEntityById(id, connection);

        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e,
                    String.format("Error during findById for %s with id: %s", this.getClass().getSimpleName(), id));
        }

    }

    protected abstract Optional<E> findEntityById(K id, Connection connection) throws SQLException;

    public void update(E entity) {

        Objects.requireNonNull(entity);

        try (var connection = dataSource.getConnection()) {

            updateEntity(entity, connection);
        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e,
                    String.format("Error during update %s: %s", this.getClass().getSimpleName(), entity));
        }

    }

    protected abstract void updateEntity(E entity, Connection connection) throws SQLException;

    public void delete(K id) {

        Objects.requireNonNull(id);

        try (var connection = dataSource.getConnection()) {

            deleteById(id, connection);

        } catch (SQLException e) {
            throw SQLiteExceptionResolver.wrapException(e,
                    String.format("Error during delete %s with id: %s", this.getClass().getSimpleName(), id));
        }
    }

    protected abstract void deleteById(K id, Connection connection);

    protected static void executeUpdateStatement(Integer id, PreparedStatement statement) throws SQLException {

        int rowsAffected = statement.executeUpdate();
        if (rowsAffected == 0) {
            throw new DaoOperationException(
                    String.format("ConversionRate with id = %d does not exist", id));
        }
    }
}