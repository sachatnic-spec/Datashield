package com.datasheild.classification;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "DataShield Data Classification Service",
        version = "1.0.0",
        description = "Data classification and DLP enforcement for DPDP compliance"
    )
)
public class DataClassificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataClassificationServiceApplication.class, args);
    }
}
