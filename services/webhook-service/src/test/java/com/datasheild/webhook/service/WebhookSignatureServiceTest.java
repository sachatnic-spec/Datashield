package com.datasheild.webhook.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignatureServiceTest {

    private final WebhookSignatureService service = new WebhookSignatureService();

    @Test
    void shouldGenerateDeterministicSignature() {
        assertThat(service.generateSignature("payload", "secret"))
                .isEqualTo(service.generateSignature("payload", "secret"));
    }

    @Test
    void shouldVerifyValidSignature() {
        String signature = service.generateSignature("payload", "secret");
        assertThat(service.verifySignature("payload", signature, "secret")).isTrue();
    }

    @Test
    void shouldRejectInvalidSignature() {
        assertThat(service.verifySignature("payload", "bad-signature", "secret")).isFalse();
    }
}
