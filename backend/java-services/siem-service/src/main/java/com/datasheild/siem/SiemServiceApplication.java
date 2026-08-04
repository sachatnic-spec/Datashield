package com.datasheild.siem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class SiemServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiemServiceApplication.class, args);
    }
}
