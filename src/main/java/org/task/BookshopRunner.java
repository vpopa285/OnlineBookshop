package org.task;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@Profile("!test")
public class BookshopRunner implements CommandLineRunner {
    private final App app;

    public BookshopRunner(App app) {
        this.app = app;
    }

    @Override
    public void run(String... args) throws SQLException {
        app.run();
    }
}
