package org.task.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.task.datasource.DataSourceImpl;
import org.task.datasource.HikariCPDataSource;

import java.sql.SQLException;
import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DatabaseConfiguration.class)
@Profile("benchmark")
public class BenchmarkDataSourceConfig {

    @Bean
    public DataSource singleConnectionDataSource(DatabaseConfiguration config) throws SQLException {
        return new DataSourceImpl(
                config.getUrl(),
                config.getUsername(),
                config.getPassword()
        );
    }

    @Bean
    public DataSource pooledBenchmarkDataSource(DatabaseConfiguration config) {
        return HikariCPDataSource.create(
                config.getUrl(),
                config.getUsername(),
                config.getPassword()
        );
    }
}
