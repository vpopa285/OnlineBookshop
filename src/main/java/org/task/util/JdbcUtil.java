package org.task.util;
import org.task.datasource.DataSourceImpl;
import org.task.datasource.HikariCPDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public final class JdbcUtil {

    private final DataSource dataSource;

    public JdbcUtil() {
        dataSource = HikariCPDataSource.create();
    }

    public JdbcUtil(DataSourceImpl dataSourceImpl) {
        dataSource = dataSourceImpl;
    }

    public JdbcUtil(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String query, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setArguments(statement, args);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute the statement", e);
        }
    }

    public void execute(String query, Consumer<PreparedStatement> statementConsumer) {
        Objects.requireNonNull(statementConsumer);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statementConsumer.accept(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute the statement", e);
        }
    }

    public <T> T findOne(String query, Function<ResultSet, T> mapper, Object... args) {
        Objects.requireNonNull(mapper);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setArguments(statement, args);

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            T result = mapper.apply(resultSet);
            if (resultSet.next()) {
                throw new RuntimeException("Expected one result at most");
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute the query", e);
        }
    }

    public <T> List<T> findMany(String query, Function<ResultSet, T> mapper, Object... args) {
        Objects.requireNonNull(mapper);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            setArguments(statement, args);

            ResultSet resultSet = statement.executeQuery();
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapper.apply(resultSet));
            }

            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute the query", e);
        }
    }

    public void simulateQuery() {
        execute("SELECT pg_sleep(1)");
    }

    private static void setArguments(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
