package org.task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.task.jdbc.JdbcExecutor;

import javax.sql.DataSource;

@Configuration
@Profile("benchmark")
public class BenchmarkJdbcConfig {

    @Bean
    public JdbcExecutor singleConnectionJdbcExecutor(
            DataSource singleConnectionDataSource) {
        return new JdbcExecutor(singleConnectionDataSource);
    }

    @Bean
    public JdbcExecutor pooledBenchmarkJdbcExecutor(
            DataSource pooledBenchmarkDataSource) {
        return new JdbcExecutor(pooledBenchmarkDataSource);
    }
}
