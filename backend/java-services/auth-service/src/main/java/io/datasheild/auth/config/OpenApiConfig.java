package io.datasheild.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT auth token")))
                .info(new Info()
                        .title("DataShield Auth Service")
                        .description("OAuth2 JWT Authentication Service for DPDP Compliance Platform")
                        .version("1.0.0-MVP")
                        .contact(new Contact()
                                .name("DataShield Engineering")
                                .email("eng@datasheild.in")));
    }
}
