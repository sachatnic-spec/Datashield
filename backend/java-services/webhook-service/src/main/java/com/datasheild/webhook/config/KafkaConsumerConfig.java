package com.datasheild.webhook.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ApplicationRunner webhookSchemaInitializer(JdbcTemplate jdbcTemplate) {
        return args -> jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS webhook");
    }
}
