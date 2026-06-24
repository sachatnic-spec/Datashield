package com.datasheild.connector.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VaultServiceTest {

    private final VaultService vaultService = new VaultService();

    @Test
    void shouldEncryptAndDecryptCredentials() {
        String encrypted = vaultService.encryptCredentials("secret-value");
        assertThat(encrypted).isNotEqualTo("secret-value");
        assertThat(vaultService.decryptCredentials(encrypted)).isEqualTo("secret-value");
    }

    @Test
    void shouldReturnBlankValueAsIs() {
        assertThat(vaultService.encryptCredentials(" ")).isEqualTo(" ");
        assertThat(vaultService.decryptCredentials(" ")).isEqualTo(" ");
    }
}
