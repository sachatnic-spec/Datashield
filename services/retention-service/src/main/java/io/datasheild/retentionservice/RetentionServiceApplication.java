package io.datasheild.retentionservice;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RetentionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetentionServiceApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("DataShield Retention Service")
                .version("1.0.0")
                .description("Data retention policy enforcement and lifecycle automation (DPDP § 11-12)"));
    }
}
