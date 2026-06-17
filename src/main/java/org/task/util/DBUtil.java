package org.task.util;

public class DBUtil {
    private static final String DB_PORT = getConfig("DB_PORT", "5432");
    private static final String DB_NAME = getConfig("DB_NAME", "librarydb");

    public static final String URL = "jdbc:postgresql://localhost:" + DB_PORT + "/" + DB_NAME;
    public static final String USER = getConfig("DB_USER", "admin");
    public static final String PASSWORD = getConfig("DB_PASSWORD", "admin");

    private static String getConfig(String key, String defaultValue) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String environmentVariable = System.getenv(key);
        if (environmentVariable != null && !environmentVariable.isBlank()) {
            return environmentVariable;
        }

        return defaultValue;
    }
}
