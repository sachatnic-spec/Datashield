package com.datasheild.discovery;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "DataShield Data Discovery Service",
        version = "1.0.0",
        description = "PII detection, discovery, and classification for DPDP compliance"
    )
)
public class DataDiscoveryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataDiscoveryServiceApplication.class, args);
    }
}
