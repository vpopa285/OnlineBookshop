package org.task.util;

import io.github.cdimascio.dotenv.Dotenv;

public class DBUtil {
    private static final Dotenv DOTENV = Dotenv.load();

    public static final String URL = "jdbc:postgresql://localhost:" + DOTENV.get("DB_PORT") + "/" + DOTENV.get("DB_NAME");
    public static final String USER = DOTENV.get("DB_USER");
    public static final String PASSWORD = DOTENV.get("DB_PASSWORD");
}
