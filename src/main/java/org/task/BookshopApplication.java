package org.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.task.config.DatabaseConfiguration;

@SpringBootApplication
@EnableConfigurationProperties(DatabaseConfiguration.class)
public class BookshopApplication {
    public static void main(String [] args) {
        SpringApplication.run(BookshopApplication.class, args);
    }
}
