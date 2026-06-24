package com.datasheild.searchservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI searchServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("DataShield Search Service")
                .description("Tenant-scoped search, indexing and aggregation APIs")
                .version("1.0.0")
                .contact(new Contact().name("DataShield")));
    }
}
