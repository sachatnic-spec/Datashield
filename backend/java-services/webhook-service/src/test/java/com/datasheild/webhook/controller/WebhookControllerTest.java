package com.datasheild.webhook.controller;

import com.datasheild.webhook.dto.WebhookEndpointRequest;
import com.datasheild.webhook.entity.WebhookEndpoint;
import com.datasheild.webhook.entity.WebhookEvent;
import com.datasheild.webhook.repository.WebhookEndpointRepository;
import com.datasheild.webhook.repository.WebhookEventRepository;
import com.datasheild.webhook.service.WebhookDeliveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock
    private WebhookEndpointRepository endpointRepository;
    @Mock
    private WebhookEventRepository eventRepository;
    @Mock
    private WebhookDeliveryService deliveryService;

    private WebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new WebhookController(endpointRepository, eventRepository, deliveryService, new ObjectMapper());
    }

    @Test
    void shouldCreateEndpoint() throws Exception {
        WebhookEndpointRequest request = new WebhookEndpointRequest();
        request.setUrl("https://example.com");
        request.setEventsSubscribed(List.of("connector.synced"));
        when(endpointRepository.save(any(WebhookEndpoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(controller.createEndpoint(request, "tenant-a").getBody().getTenantId()).isEqualTo("tenant-a");
    }

    @Test
    void shouldListEndpoints() {
        when(endpointRepository.findByTenantId(eq("tenant-a"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(WebhookEndpoint.builder().tenantId("tenant-a").build())));
        assertThat(controller.listEndpoints("tenant-a", 0, 10).getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldRetryEvent() {
        WebhookEvent event = WebhookEvent.builder().id(5L).build();
        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        when(deliveryService.deliver(event)).thenReturn(event);
        assertThat(controller.retryEvent(5L).getStatusCode().value()).isEqualTo(202);
    }
}
