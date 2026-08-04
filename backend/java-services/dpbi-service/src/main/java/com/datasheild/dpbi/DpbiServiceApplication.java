package com.datasheild.dpbi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class DpbiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DpbiServiceApplication.class, args);
    }
}
