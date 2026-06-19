package org.task.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "spring.datasource")
public class DatabaseConfiguration {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
