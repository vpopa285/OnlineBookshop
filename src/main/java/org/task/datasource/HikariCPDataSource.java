package org.task.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.task.util.DBUtil;

import javax.sql.DataSource;

public class HikariCPDataSource {
    public static DataSource create() {
        return create(DBUtil.URL, DBUtil.USER, DBUtil.PASSWORD);
    }

    public static DataSource create(String url, String username, String password) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(16);
        config.setInitializationFailTimeout(-1);

        return new HikariDataSource(config);
    }
}
