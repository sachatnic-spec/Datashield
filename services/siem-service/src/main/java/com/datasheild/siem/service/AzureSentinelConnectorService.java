package com.datasheild.siem.service;

import com.datasheild.siem.config.SiemProperties;
import com.datasheild.siem.entity.SiemAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureSentinelConnectorService {

    private final SiemProperties properties;
    private final RestTemplate restTemplate;

    public void postEvent(SiemAlert alert) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(properties.getSentinel().getHecToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(properties.getSentinel().getHecUrl(),
                    new HttpEntity<>(Map.of("alertType", alert.getAlertType(), "message", alert.getMessage(), "severity", alert.getSeverity()), headers),
                    String.class);
        } catch (Exception ex) {
            log.error("Sentinel post failed", ex);
        }
    }
}
