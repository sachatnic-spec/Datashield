package com.datasheild.configservice.service;

import com.datasheild.configservice.config.VaultProperties;
import com.datasheild.configservice.dto.SecretRequest;
import com.datasheild.configservice.dto.SecretResponse;
import com.datasheild.configservice.entity.ConfigStatus;
import com.datasheild.configservice.entity.SecretConfig;
import com.datasheild.configservice.exception.CryptoOperationException;
import com.datasheild.configservice.exception.ResourceNotFoundException;
import com.datasheild.configservice.repository.SecretConfigRepository;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecretVaultService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretConfigRepository secretConfigRepository;
    private final VaultProperties vaultProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    private SecretKeySpec keySpec;

    @PostConstruct
    void initialize() {
        keySpec = deriveKey(vaultProperties.getKey());
    }

    public SecretResponse getSecret(String key) {
        SecretConfig secretConfig = secretConfigRepository.findBySecretKeyAndStatus(key, ConfigStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Secret not found for key " + key));
        return new SecretResponse(
                secretConfig.getSecretKey(),
                decrypt(secretConfig.getEncryptedValue()),
                secretConfig.getKeyVersion(),
                secretConfig.getUpdatedAt());
    }

    @Transactional
    public SecretResponse storeSecret(SecretRequest request) {
        SecretConfig secretConfig = secretConfigRepository.findBySecretKey(request.key())
                .orElseGet(() -> SecretConfig.builder().secretKey(request.key()).build());
        secretConfig.setEncryptedValue(encrypt(request.secretValue()));
        secretConfig.setKeyVersion(vaultProperties.getKeyVersion());
        secretConfig.setStatus(ConfigStatus.ACTIVE);
        SecretConfig saved = secretConfigRepository.save(secretConfig);
        return new SecretResponse(saved.getSecretKey(), request.secretValue(), saved.getKeyVersion(), saved.getUpdatedAt());
    }

    private String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv) + "." + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception exception) {
            throw new CryptoOperationException("Failed to encrypt secret", exception);
        }
    }

    private String decrypt(String cipherText) {
        try {
            String[] parts = cipherText.split("\\.", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Malformed encrypted payload");
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new CryptoOperationException("Failed to decrypt secret", exception);
        }
    }

    private SecretKeySpec deriveKey(String configuredKey) {
        try {
            byte[] rawKey;
            try {
                byte[] decoded = Base64.getDecoder().decode(configuredKey);
                rawKey = isValidAesKeyLength(decoded.length)
                        ? decoded
                        : hash(configuredKey.getBytes(StandardCharsets.UTF_8));
            } catch (IllegalArgumentException exception) {
                rawKey = hash(configuredKey.getBytes(StandardCharsets.UTF_8));
            }
            return new SecretKeySpec(Arrays.copyOf(rawKey, 32), "AES");
        } catch (NoSuchAlgorithmException exception) {
            throw new CryptoOperationException("Failed to derive vault key", exception);
        }
    }

    private boolean isValidAesKeyLength(int length) {
        return length == 16 || length == 24 || length == 32;
    }

    private byte[] hash(byte[] input) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(input);
    }
}
