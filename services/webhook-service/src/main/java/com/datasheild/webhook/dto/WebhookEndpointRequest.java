package com.datasheild.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class WebhookEndpointRequest {

    @NotBlank
    private String url;

    @NotEmpty
    private List<String> eventsSubscribed;

    private Boolean active = true;
}
