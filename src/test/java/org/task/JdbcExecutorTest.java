package org.task;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.h2.jdbcx.JdbcDataSource;
import org.task.jdbc.JdbcExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcExecutorTest {
    private static JdbcExecutor jdbcExecutor;

    @BeforeAll
    static void setUpDatabase() {
        DatabaseTestSupport.runInitScript();
        jdbcExecutor = DatabaseTestSupport.JDBC_EXECUTOR;
    }

    @Test
    void dataSourceConstructorTest() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(DatabaseTestSupport.URL);
        dataSource.setUser(DatabaseTestSupport.USERNAME);
        dataSource.setPassword(DatabaseTestSupport.PASSWORD);

        assertThatCode(() -> new JdbcExecutor(dataSource)).doesNotThrowAnyException();
    }

    @Test
    void executeWithArgumentsTest() {
        jdbcExecutor.execute("INSERT INTO users (id, username, email, password, amount, restriction) VALUES (?, ?, ?, ?, ?, ?)",
                101, "jdbc_user", "jdbc@example.com", "secret", 75, false
        );

        String username = jdbcExecutor.findOne(
                "SELECT username FROM users WHERE id = ?",
                resultSet -> getString(resultSet, "username"),
                101
        ).get();

        assertThat(username).isEqualTo("jdbc_user");
    }

    @Test
    void executeWithConsumerTest() {
        jdbcExecutor.execute("INSERT INTO books (id, title, author, genre, content, price) VALUES (?, ?, ?, ?, ?, ?)",
                statement -> {
                    try {
                        statement.setLong(1, 102);
                        statement.setString(2, "Consumer Book");
                        statement.setString(3, "Test Author");
                        statement.setString(4, "Testing");
                        statement.setString(5, "Bound through a consumer.");
                        statement.setDouble(6, 18.50);
                    } catch (SQLException e) {
                        throw new IllegalStateException(e);
                    }
                }
        );

        Double price = jdbcExecutor.findOne(
                "SELECT price FROM books WHERE id = ?",
                resultSet -> getDouble(resultSet),
                102
        ).get();

        assertThat(price).isEqualTo(18.50);
    }

    @Test
    void findOneTest() {
        String result = jdbcExecutor.findOne("SELECT username FROM users WHERE id = ?",
                resultSet -> getString(resultSet, "username"),
                -1
        ).orElse(null);

        assertThat(result).isNull();
    }

    @Test
    void findOneExceptionTest() {
        assertThatThrownBy(() -> jdbcExecutor.findOne("SELECT username FROM users ORDER BY id",
                resultSet -> getString(resultSet, "username")
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expected one result at most");
    }

    @Test
    void findOneSqlExceptionTest() {
        assertThatThrownBy(() -> jdbcExecutor.findOne("SELECT missing_column FROM users",
                resultSet -> getString(resultSet, "missing_column")
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to execute the query");
    }

    @Test
    void findManyTest() {
        List<String> titles = jdbcExecutor.findMany("SELECT title FROM books WHERE price >= ? ORDER BY title",
                resultSet -> getString(resultSet, "title"),
                12
        );

        assertThat(titles).contains("Clean Code", "The Great Gatsby");
    }

    @Test
    void findManyEmptyListTest() {
        List<String> results = jdbcExecutor.findMany("SELECT title FROM books WHERE author = ?",
                resultSet -> getString(resultSet, "title"),
                "Nobody"
        );

        assertThat(results).isEmpty();
    }

    @Test
    void findManySqlExceptionTest() {
        assertThatThrownBy(() -> jdbcExecutor.findMany("SELECT missing_column FROM books",
                resultSet -> getString(resultSet, "missing_column")
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to execute the query");
    }

    @Test
    void wrapSqlErrorsTest() {
        assertThatThrownBy(() -> jdbcExecutor.execute("INSERT INTO users (id, username, email, password) VALUES (?, ?, ?, ?)",
                1, "duplicate", "duplicate@example.com", "secret"
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to execute the statement");
    }

    private static String getString(ResultSet resultSet, String column) {
        try {
            return resultSet.getString(column);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static double getDouble(ResultSet resultSet) {
        try {
            return resultSet.getDouble("price");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
