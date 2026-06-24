package com.datasheild.configservice.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "datasheild.vault")
public class VaultProperties {

    @NotBlank
    private String key;

    @Min(1)
    private Integer keyVersion = 1;
}
