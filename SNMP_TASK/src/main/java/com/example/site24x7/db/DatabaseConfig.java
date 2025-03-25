package com.example.site24x7.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.ServletContextEvent;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;

public class DatabaseConfig {
    private static HikariDataSource dataSource;
    private static CqlSession cassandraSession;

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("hostdetails.properties")) {
            if (input == null) {
                throw new RuntimeException("Database properties file not found!");
            }

            Properties properties = new Properties();
            properties.load(input);

            // Setting up HikariCP for relational database
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.username"));
            config.setPassword(properties.getProperty("db.password"));
            config.setDriverClassName(properties.getProperty("db.driver"));

            config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.poolsize")));
            config.setMinimumIdle(Integer.parseInt(properties.getProperty("db.minIdle")));
            config.setIdleTimeout(Long.parseLong(properties.getProperty("db.maxWaitMillis")));
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);

            cassandraSession = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(
                        properties.getProperty("CASSANDRA_HOST"),
                        Integer.parseInt(properties.getProperty("CASSANDRA_PORT"))
                ))
                .withKeyspace(properties.getProperty("KEYSPACE"))
                .withLocalDatacenter("datacenter1")
                .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load database properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static CqlSession getCassandraSession() {
        return cassandraSession;
    }
    
   

    public static void closeDataSources() {
        if (dataSource != null) {
            dataSource.close();
        }
        if (cassandraSession != null) {
            cassandraSession.close();
        }
    }
    public void contextDestroyed(ServletContextEvent sce) {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
