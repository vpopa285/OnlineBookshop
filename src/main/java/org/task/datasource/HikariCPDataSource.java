package org.task.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.task.util.DBUtil;

import javax.sql.DataSource;

public class HikariCPDataSource {
    public static DataSource create() {

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(DBUtil.URL);
        config.setUsername(DBUtil.USER);
        config.setPassword(DBUtil.PASSWORD);

        config.setMaximumPoolSize(16);

        return new HikariDataSource(config);
    }
}
