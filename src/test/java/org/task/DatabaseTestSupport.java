package org.task;

import org.h2.jdbcx.JdbcDataSource;
import org.task.jdbc.JdbcExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;

final class DatabaseTestSupport {
    static final String URL = "jdbc:h2:mem:bookshop;MODE=PostgreSQL;"
            + "DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1";
    static final String USERNAME = "sa";
    static final String PASSWORD = "";

    static JdbcExecutor JDBC_EXECUTOR;

    private static DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();

        dataSource.setUrl(URL);
        dataSource.setUser(USERNAME);
        dataSource.setPassword(PASSWORD);

        return dataSource;
    }

    static void runInitScript() {
        DataSource dataSource = dataSource();
        JDBC_EXECUTOR = new JdbcExecutor(dataSource);

        for (String statement : readInitScript().split(";")) {
            if (!statement.isBlank()) {
                JDBC_EXECUTOR.execute(statement);
            }
        }
    }

    private static String readInitScript() {
        try (InputStream inputStream = DatabaseTestSupport.class
                .getResourceAsStream("/db/init.sql")) {
            if (inputStream == null) {
                throw new RuntimeException("Missing init");
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed init", e);
        }
    }
}
