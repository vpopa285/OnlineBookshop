package org.task.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.task.datasource.HikariCPDataSource;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DatabaseConfiguration.class)
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(DatabaseConfiguration config) {
        return HikariCPDataSource.create(
                config.getUrl(),
                config.getUsername(),
                config.getPassword()
        );
    }
}
