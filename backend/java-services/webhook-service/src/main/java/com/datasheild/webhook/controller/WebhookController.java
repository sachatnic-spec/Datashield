package com.datasheild.webhook.controller;

import com.datasheild.webhook.dto.WebhookEndpointRequest;
import com.datasheild.webhook.entity.WebhookEndpoint;
import com.datasheild.webhook.entity.WebhookEvent;
import com.datasheild.webhook.repository.WebhookEndpointRepository;
import com.datasheild.webhook.repository.WebhookEventRepository;
import com.datasheild.webhook.service.WebhookDeliveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookEventRepository eventRepository;
    private final WebhookDeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @PostMapping("/endpoints")
    public ResponseEntity<WebhookEndpoint> createEndpoint(@Valid @RequestBody WebhookEndpointRequest request,
                                                          @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) throws Exception {
        WebhookEndpoint endpoint = WebhookEndpoint.builder()
                .tenantId(tenantId)
                .url(request.getUrl())
                .eventsSubscribed(objectMapper.writeValueAsString(request.getEventsSubscribed()))
                .isActive(request.getActive())
                .secret(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.ok(endpointRepository.save(endpoint));
    }

    @GetMapping("/endpoints")
    public ResponseEntity<Page<WebhookEndpoint>> listEndpoints(@RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(endpointRepository.findByTenantId(tenantId, PageRequest.of(page, size)));
    }

    @DeleteMapping("/endpoints/{id}")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable Long id,
                                               @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) {
        WebhookEndpoint endpoint = endpointRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Webhook endpoint not found"));
        endpointRepository.delete(endpoint);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/retry/{eventId}")
    public ResponseEntity<WebhookEvent> retryEvent(@PathVariable Long eventId) {
        WebhookEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Webhook event not found"));
        return ResponseEntity.accepted().body(deliveryService.deliver(event));
    }
}
