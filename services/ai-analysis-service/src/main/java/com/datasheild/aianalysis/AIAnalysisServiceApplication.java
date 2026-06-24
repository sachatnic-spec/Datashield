package com.datasheild.aianalysis;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "DataShield AI Analysis Service",
        version = "1.0.0",
        description = "Anomaly detection and trend forecasting for DPDP compliance"
    )
)
public class AIAnalysisServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AIAnalysisServiceApplication.class, args);
    }
}
