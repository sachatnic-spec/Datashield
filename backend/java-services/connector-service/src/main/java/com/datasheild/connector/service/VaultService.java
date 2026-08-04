package com.datasheild.connector.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class VaultService {

    public String encryptCredentials(String plaintext) {
        if (!StringUtils.hasText(plaintext)) {
            return plaintext;
        }
        return Base64.getEncoder().encodeToString(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    public String decryptCredentials(String encrypted) {
        if (!StringUtils.hasText(encrypted)) {
            return encrypted;
        }
        return new String(Base64.getDecoder().decode(encrypted), StandardCharsets.UTF_8);
    }
}
