# Autoconfiguration
## Debugging AutoConfiguration

AutoConfiguration report is enabled by adding:

debug=true

to application.properties.

After application startup Spring prints CONDITIONS EVALUATION REPORT,
which shows which auto-configurations were applied and which were skipped.

## Disabling AutoConfiguration

AutoConfiguration is disabled using:

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)

or

spring.autoconfigure.exclude=\org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

in application.properties