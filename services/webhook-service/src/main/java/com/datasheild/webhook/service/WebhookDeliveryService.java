package com.datasheild.webhook.service;

import com.datasheild.webhook.entity.WebhookDeadLetter;
import com.datasheild.webhook.entity.WebhookEndpoint;
import com.datasheild.webhook.entity.WebhookEvent;
import com.datasheild.webhook.entity.WebhookRetry;
import com.datasheild.webhook.repository.WebhookDeadLetterRepository;
import com.datasheild.webhook.repository.WebhookEndpointRepository;
import com.datasheild.webhook.repository.WebhookEventRepository;
import com.datasheild.webhook.repository.WebhookRetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebhookDeliveryService {

    private final RestTemplate restTemplate;
    private final WebhookSignatureService signatureService;
    private final WebhookEndpointRepository endpointRepository;
    private final WebhookEventRepository eventRepository;
    private final WebhookRetryRepository retryRepository;
    private final WebhookDeadLetterRepository deadLetterRepository;

    public WebhookEvent deliver(WebhookEvent event) {
        WebhookEndpoint endpoint = endpointRepository.findById(event.getEndpointId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Webhook endpoint not found"));
        String signature = signatureService.generateSignature(event.getPayload(), endpoint.getSecret());
        event.setLastAttemptAt(LocalDateTime.now());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Webhook-Signature", signature);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint.getUrl(), new HttpEntity<>(event.getPayload(), headers), String.class);
            event.setResponseCode(response.getStatusCode().value());
            if (response.getStatusCode().is2xxSuccessful()) {
                event.setStatus("DELIVERED");
                event.setFailureReason(null);
            } else {
                scheduleRetry(event, "Unexpected response: " + response.getStatusCode().value());
            }
        } catch (Exception ex) {
            log.warn("Webhook delivery failed for event {}", event.getId(), ex);
            scheduleRetry(event, ex.getMessage());
        }
        return eventRepository.save(event);
    }

    private void scheduleRetry(WebhookEvent event, String reason) {
        int nextRetry = event.getRetryCount() + 1;
        event.setRetryCount(nextRetry);
        event.setFailureReason(reason);
        long delaySeconds = (long) Math.pow(2, nextRetry);
        if (delaySeconds > 128) {
            event.setStatus("DEAD_LETTER");
            deadLetterRepository.save(WebhookDeadLetter.builder()
                    .eventId(event.getId())
                    .reason("Max retries exceeded")
                    .payload(event.getPayload())
                    .build());
            return;
        }
        event.setStatus("FAILED");
        event.setNextAttemptAt(LocalDateTime.now().plusSeconds(delaySeconds));
        retryRepository.save(WebhookRetry.builder()
                .eventId(event.getId())
                .attemptNumber(nextRetry)
                .nextAttemptAt(event.getNextAttemptAt())
                .lastError(reason)
                .build());
    }
}
