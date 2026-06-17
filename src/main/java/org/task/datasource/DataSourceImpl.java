package org.task.datasource;

import org.task.util.DBUtil;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class DataSourceImpl implements DataSource {
    private final Connection connection;

    public DataSourceImpl() throws SQLException {
        connection = DriverManager.getConnection(DBUtil.URL, DBUtil.USER, DBUtil.PASSWORD);
    }

    public DataSourceImpl(String url, String username, String password) throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
    }

    @Override
    public synchronized Connection getConnection() {
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) {
        return connection;
    }

    // You can leave all the other methods unimplemented
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
