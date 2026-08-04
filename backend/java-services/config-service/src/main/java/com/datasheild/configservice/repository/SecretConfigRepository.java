package com.datasheild.configservice.repository;

import com.datasheild.configservice.entity.ConfigStatus;
import com.datasheild.configservice.entity.SecretConfig;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecretConfigRepository extends JpaRepository<SecretConfig, UUID> {

    Optional<SecretConfig> findBySecretKeyAndStatus(String secretKey, ConfigStatus status);

    Optional<SecretConfig> findBySecretKey(String secretKey);
}
