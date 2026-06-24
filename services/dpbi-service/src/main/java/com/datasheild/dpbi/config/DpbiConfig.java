package com.datasheild.dpbi.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(DpbiProperties.class)
public class DpbiConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ApplicationRunner dpbiSchemaInitializer(JdbcTemplate jdbcTemplate) {
        return args -> jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS dpbi");
    }
}
