package com.datasheild.configservice.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SchemaInitializer {

    @Bean
    public ApplicationRunner initializeSchema(JdbcTemplate jdbcTemplate) {
        return args -> jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS config");
    }
}
