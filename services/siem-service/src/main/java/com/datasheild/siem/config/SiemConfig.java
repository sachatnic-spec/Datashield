package com.datasheild.siem.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(SiemProperties.class)
public class SiemConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ApplicationRunner siemSchemaInitializer(JdbcTemplate jdbcTemplate) {
        return args -> jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS siem");
    }
}
