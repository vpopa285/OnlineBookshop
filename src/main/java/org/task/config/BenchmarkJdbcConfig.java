package org.task.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.task.util.JdbcUtil;

import javax.sql.DataSource;

@Configuration
@Profile("benchmark")
public class BenchmarkJdbcConfig {

    @Bean
    public JdbcUtil singleConnectionJdbcUtil(
            @Qualifier("singleConnectionDataSource") DataSource dataSource) {
        return new JdbcUtil(dataSource);
    }

    @Bean
    public JdbcUtil pooledBenchmarkJdbcUtil(
            @Qualifier("pooledBenchmarkDataSource") DataSource dataSource) {
        return new JdbcUtil(dataSource);
    }
}
