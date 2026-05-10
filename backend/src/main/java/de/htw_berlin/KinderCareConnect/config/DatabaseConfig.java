package de.htw_berlin.KinderCareConnect.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Database Configuration for PostgreSQL
 * Optimizes connection pooling and performance settings for Render deployment
 */
@Configuration
public class DatabaseConfig {

    @Bean
    @Profile({"production", "postgresql"})
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource configuredDataSource(DataSourceProperties properties) {
        HikariDataSource dataSource = properties
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();

        if (dataSource.getMaximumPoolSize() < 1) {
            dataSource.setMaximumPoolSize(5);
        }

        return dataSource;
    }
}

