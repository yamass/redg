package de.yamass.redg.generator.testutil;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;

public final class DataSourceFactory {

    private DataSourceFactory() {}

    public static DataSource create(JdbcDatabaseContainer<?> container) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(container.getJdbcUrl());
        cfg.setUsername(container.getUsername());
        cfg.setPassword(container.getPassword());
        cfg.setDriverClassName(container.getDriverClassName());
        cfg.setMaximumPoolSize(2); // modest for tests
        return new HikariDataSource(cfg);
    }
}