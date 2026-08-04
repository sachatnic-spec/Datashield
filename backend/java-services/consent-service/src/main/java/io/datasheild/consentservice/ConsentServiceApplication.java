package io.datasheild.consentservice;

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
        title = "DataShield Consent Service API",
        version = "1.0.0",
        description = "DPDP Act 2023 § 6-7 Consent Management Service",
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
public class ConsentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsentServiceApplication.class, args);
    }
}
