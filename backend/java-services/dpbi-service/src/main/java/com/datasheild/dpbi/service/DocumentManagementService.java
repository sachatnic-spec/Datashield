package com.datasheild.dpbi.service;

import com.datasheild.dpbi.config.DpbiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DocumentManagementService {

    private final DpbiProperties properties;
    private final Map<String, byte[]> storage = new ConcurrentHashMap<>();

    public String uploadDocument(Long formId, String fileName, byte[] content) {
        String key = properties.getS3().getBucketName() + "/" + formId + "/" + fileName;
        storage.put(key, content);
        return key;
    }

    public byte[] downloadDocument(String key) {
        return storage.getOrDefault(key, new byte[0]);
    }
}
