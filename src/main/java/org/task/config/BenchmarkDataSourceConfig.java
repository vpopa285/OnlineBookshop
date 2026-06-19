package org.task.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.task.datasource.DataSourceImpl;
import org.task.datasource.HikariCPDataSource;

import java.sql.SQLException;
import javax.sql.DataSource;

@Configuration
@Profile("benchmark")
public class BenchmarkDataSourceConfig {

    @Bean
    public DataSource singleConnectionDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) throws SQLException {
        return new DataSourceImpl(url, username, password);
    }

    @Bean
    public DataSource pooledBenchmarkDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        return HikariCPDataSource.create(url, username, password);
    }
}
