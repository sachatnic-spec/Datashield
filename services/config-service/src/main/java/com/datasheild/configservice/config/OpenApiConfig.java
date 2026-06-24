package com.datasheild.configservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI configServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Config Service API")
                .description("Tenant configuration, feature flag, and secret management APIs")
                .version("v1"));
    }
}
