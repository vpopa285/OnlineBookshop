package org.task.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.task.config.DatabaseConfiguration;

@Component
public class DBUtil {
    DatabaseConfiguration configuration;

    public static String URL;
    public static String USER;
    public static String PASSWORD;

    @Autowired
    public DBUtil(DatabaseConfiguration configuration) {
        this.configuration = configuration;
        URL = configuration.getUrl();
        USER = configuration.getUsername();
        PASSWORD = configuration.getPassword();
    }
}
