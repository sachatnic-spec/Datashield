package com.datasheild.connector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConnectorRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String connectorType;

    @NotBlank
    private String sourceType;

    @NotBlank
    private String targetType;

    @NotBlank
    private String endpoint;

    @NotBlank
    private String credentials;

    private String configurationJson;
}
