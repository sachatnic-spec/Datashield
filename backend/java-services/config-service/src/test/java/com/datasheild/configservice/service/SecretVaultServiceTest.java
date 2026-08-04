package com.datasheild.configservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.datasheild.configservice.config.VaultProperties;
import com.datasheild.configservice.dto.SecretRequest;
import com.datasheild.configservice.dto.SecretResponse;
import com.datasheild.configservice.entity.ConfigStatus;
import com.datasheild.configservice.entity.SecretConfig;
import com.datasheild.configservice.repository.SecretConfigRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SecretVaultServiceTest {

    @Mock
    private SecretConfigRepository secretConfigRepository;

    private SecretVaultService secretVaultService;

    @BeforeEach
    void setUp() {
        VaultProperties vaultProperties = new VaultProperties();
        vaultProperties.setKey("change-me-config-service-local-key");
        vaultProperties.setKeyVersion(1);
        secretVaultService = new SecretVaultService(secretConfigRepository, vaultProperties);
        secretVaultService.initialize();
    }

    @Test
    void shouldEncryptOnSaveAndDecryptOnRead() {
        SecretConfig[] persisted = new SecretConfig[1];
        when(secretConfigRepository.findBySecretKey("siem.token")).thenReturn(Optional.empty());
        when(secretConfigRepository.save(any(SecretConfig.class))).thenAnswer(invocation -> {
            persisted[0] = invocation.getArgument(0);
            persisted[0].setUpdatedAt(LocalDateTime.now());
            return persisted[0];
        });

        SecretResponse stored = secretVaultService.storeSecret(new SecretRequest("siem.token", "super-secret"));
        assertThat(stored.secretValue()).isEqualTo("super-secret");
        assertThat(persisted[0].getEncryptedValue()).isNotEqualTo("super-secret");

        when(secretConfigRepository.findBySecretKeyAndStatus("siem.token", ConfigStatus.ACTIVE))
                .thenReturn(Optional.of(persisted[0]));

        SecretResponse loaded = secretVaultService.getSecret("siem.token");
        assertThat(loaded.secretValue()).isEqualTo("super-secret");
    }
}
