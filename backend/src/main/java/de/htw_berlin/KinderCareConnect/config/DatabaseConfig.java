package de.htw_berlin.KinderCareConnect.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

    /**
     * Configures HikariCP connection pool for production environment
     * This bean is used when spring.profiles.active includes 'production'
     */
    @Bean
    @Profile("production")
    public DataSource productionDataSource() {
        HikariConfig config = new HikariConfig();

        // Connection pool settings optimized for Render's resource limits
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(900000);

        // Performance settings
        config.setLeakDetectionThreshold(60000);
        config.setAutoCommit(true);

        return new HikariDataSource(config);
    }

    /**
     * Configures HikariCP connection pool for development environment
     * This bean is used for local development with PostgreSQL
     */
    @Bean
    @Profile("postgresql")
    public DataSource postgresqlDataSource() {
        HikariConfig config = new HikariConfig();

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        config.setAutoCommit(true);

        return new HikariDataSource(config);
    }
}

