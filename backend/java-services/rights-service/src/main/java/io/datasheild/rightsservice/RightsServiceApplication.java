package io.datasheild.rightsservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableKafka
@OpenAPIDefinition(
    info = @Info(
        title = "DataShield Rights Service API",
        version = "1.0.0",
        description = "DPDP Act 2023 § 13-§22 Data Subject Rights Management",
        license = @License(name = "Proprietary")
    )
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "Enter JWT bearer token"
)
public class RightsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RightsServiceApplication.class, args);
    }
}
