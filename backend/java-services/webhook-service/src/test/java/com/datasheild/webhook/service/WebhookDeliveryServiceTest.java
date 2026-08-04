package com.datasheild.webhook.service;

import com.datasheild.webhook.entity.WebhookEndpoint;
import com.datasheild.webhook.entity.WebhookEvent;
import com.datasheild.webhook.repository.WebhookDeadLetterRepository;
import com.datasheild.webhook.repository.WebhookEndpointRepository;
import com.datasheild.webhook.repository.WebhookEventRepository;
import com.datasheild.webhook.repository.WebhookRetryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private WebhookEndpointRepository endpointRepository;
    @Mock
    private WebhookEventRepository eventRepository;
    @Mock
    private WebhookRetryRepository retryRepository;
    @Mock
    private WebhookDeadLetterRepository deadLetterRepository;

    private WebhookDeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new WebhookDeliveryService(restTemplate, new WebhookSignatureService(), endpointRepository,
                eventRepository, retryRepository, deadLetterRepository);
    }

    @Test
    void shouldMarkDeliveredOnSuccess() {
        WebhookEvent event = WebhookEvent.builder().id(1L).endpointId(2L).payload("{}").retryCount(0).status("PENDING").build();
        WebhookEndpoint endpoint = WebhookEndpoint.builder().id(2L).url("https://example.com").secret("secret").build();
        when(endpointRepository.findById(2L)).thenReturn(Optional.of(endpoint));
        when(restTemplate.postForEntity(eq("https://example.com"), any(), eq(String.class))).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
        when(eventRepository.save(any(WebhookEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WebhookEvent delivered = deliveryService.deliver(event);
        assertThat(delivered.getStatus()).isEqualTo("DELIVERED");
    }

    @Test
    void shouldScheduleRetryOnException() {
        WebhookEvent event = WebhookEvent.builder().id(1L).endpointId(2L).payload("{}").retryCount(0).status("PENDING").build();
        WebhookEndpoint endpoint = WebhookEndpoint.builder().id(2L).url("https://example.com").secret("secret").build();
        when(endpointRepository.findById(2L)).thenReturn(Optional.of(endpoint));
        when(restTemplate.postForEntity(eq("https://example.com"), any(), eq(String.class))).thenThrow(new RuntimeException("timeout"));
        when(eventRepository.save(any(WebhookEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WebhookEvent failed = deliveryService.deliver(event);
        assertThat(failed.getStatus()).isEqualTo("FAILED");
        verify(retryRepository).save(any());
    }

    @Test
    void shouldSendToDeadLetterAfterMaxRetryWindow() {
        WebhookEvent event = WebhookEvent.builder().id(1L).endpointId(2L).payload("{}").retryCount(7).status("FAILED").build();
        WebhookEndpoint endpoint = WebhookEndpoint.builder().id(2L).url("https://example.com").secret("secret").build();
        when(endpointRepository.findById(2L)).thenReturn(Optional.of(endpoint));
        when(restTemplate.postForEntity(eq("https://example.com"), any(), eq(String.class))).thenThrow(new RuntimeException("timeout"));
        when(eventRepository.save(any(WebhookEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WebhookEvent failed = deliveryService.deliver(event);
        assertThat(failed.getStatus()).isEqualTo("DEAD_LETTER");
        verify(deadLetterRepository).save(any());
    }
}
