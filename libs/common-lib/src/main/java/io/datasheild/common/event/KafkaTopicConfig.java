package io.datasheild.common.event;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(KafkaAdmin.AdminClientProperty.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    }

    @Bean
    public KafkaAdmin.NewTopics topics() {
        return new KafkaAdmin.NewTopics(
            createTopic("policy-activated", 3, (short) 2),
            createTopic("vendor-onboarded", 3, (short) 2),
            createTopic("data-retention-scheduled", 3, (short) 2),
            createTopic("data-erasure-completed", 3, (short) 2),
            createTopic("grievance-filed", 3, (short) 2),
            createTopic("grievance-resolved", 3, (short) 2),
            createTopic("sla-breach-alert", 3, (short) 2),
            createTopic("consent-granted", 3, (short) 2),
            createTopic("dpr-submitted", 3, (short) 2),
            createTopic("breach-reported", 3, (short) 2),
            createTopic("tenant-provisioned", 3, (short) 2),
            createTopic("workflow-completed", 3, (short) 2)
        );
    }

    private NewTopic createTopic(String name, int partitions, short replicationFactor) {
        return new NewTopic(name, partitions, replicationFactor)
            .configs(java.util.Map.of(
                "retention.ms", "604800000",
                "compression.type", "snappy",
                "cleanup.policy", "delete"
            ));
    }
}
