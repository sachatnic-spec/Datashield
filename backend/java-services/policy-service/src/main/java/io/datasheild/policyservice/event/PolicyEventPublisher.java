package io.datasheild.policyservice.event;

import io.datasheild.common.event.PolicyActivatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPolicyActivated(UUID policyId, String policyName, String category, String correlationId) {
        log.info("Publishing PolicyActivatedEvent: {}", policyId);

        PolicyActivatedEvent event = PolicyActivatedEvent.builder()
            .policyId(policyId)
            .policyName(policyName)
            .category(category)
            .activatedAt(LocalDateTime.now())
            .correlationId(correlationId)
            .build();

        kafkaTemplate.send("policy-activated", policyId.toString(), event);
        log.info("PolicyActivatedEvent published: {}", policyId);
    }
}
