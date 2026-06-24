package io.datasheild.tenantservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "DataShield Tenant Service API",
        version = "1.0.0",
        description = "Multi-tenant provisioning, configuration, and feature management",
        contact = @Contact(
            name = "DataShield Support",
            url = "https://datashield.io",
            email = "support@datashield.io"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8007",
            description = "Local Development"
        ),
        @Server(
            url = "https://tenant-api.datashield.io",
            description = "Production"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT Bearer token for API authentication",
    in = SecuritySchemeIn.HEADER
)
public class TenantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TenantServiceApplication.class, args);
    }
}
