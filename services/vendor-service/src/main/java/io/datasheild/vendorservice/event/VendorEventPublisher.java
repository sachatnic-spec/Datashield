package io.datasheild.vendorservice.event;

import io.datasheild.common.event.VendorOnboardedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class VendorEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishVendorOnboarded(UUID vendorId,
                                       String vendorName,
                                       String vendorType,
                                       String correlationId) {

        log.info("Publishing VendorOnboardedEvent: {}", vendorId);

        VendorOnboardedEvent event = VendorOnboardedEvent.builder()
                .vendorId(vendorId)
                .vendorName(vendorName)
                .vendorType(vendorType)
                .onboardedAt(LocalDateTime.now())
                .correlationId(correlationId)
                .build();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send("vendor-onboarded", vendorId.toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("VendorOnboardedEvent published successfully for vendorId={}", vendorId);
            } else {
                log.error("Failed to publish VendorOnboardedEvent for vendorId={}", vendorId, ex);
            }
        });
    }
}