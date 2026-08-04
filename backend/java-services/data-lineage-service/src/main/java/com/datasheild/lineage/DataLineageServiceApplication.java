package com.datasheild.lineage;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "DataShield Data Lineage Service",
        version = "1.0.0",
        description = "Data lineage and provenance tracking for DPDP compliance"
    )
)
public class DataLineageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataLineageServiceApplication.class, args);
    }
}
