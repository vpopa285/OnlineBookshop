package org.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.task.config.DatabaseConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DatabaseConfiguration.class)
public class BookshopApplication {
    public static void main(String [] args) {
        SpringApplication.run(BookshopApplication.class, args);
    }
}
