package com.example.lock.config;

import com.example.lock.repository.LockRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LockDataSourceConfig {
    @Bean
    @ConfigurationProperties("lock.datasource")
    public DataSource lockDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public LockRepository lockRepository() {
        return new LockRepository(lockDataSource());
    }
}
