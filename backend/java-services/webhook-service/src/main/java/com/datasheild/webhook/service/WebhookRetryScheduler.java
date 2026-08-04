package com.datasheild.webhook.service;

import com.datasheild.webhook.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WebhookRetryScheduler {

    private final WebhookEventRepository eventRepository;
    private final WebhookDeliveryService deliveryService;

    @Scheduled(fixedDelay = 30000)
    public void retryFailedEvents() {
        eventRepository.findTop20ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc("FAILED", LocalDateTime.now())
                .forEach(deliveryService::deliver);
    }
}
