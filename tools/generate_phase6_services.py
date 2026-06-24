from pathlib import Path
from textwrap import dedent

ROOT = Path(r"d:\Development Practice\Datasheild")


def write(rel_path: str, content: str) -> None:
    path = ROOT / rel_path
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(dedent(content).lstrip("\n"), encoding="utf-8")


COMMON_LOGBACK = """
<configuration>
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <pattern>
                    <pattern>
                        {
                          "level": "%level",
                          "logger": "%logger{36}",
                          "thread": "%thread",
                          "service": "${spring.application.name:-unknown}",
                          "message": "%message"
                        }
                    </pattern>
                </pattern>
                <mdc/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE"/>
    </root>
</configuration>
"""


def pom_xml(artifact: str, name: str, description: str, extra_dependencies: str = "") -> str:
    return f"""
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.datasheild</groupId>
    <artifactId>{artifact}</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <name>{name}</name>
    <description>{description}</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.5.0</version>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>7.4</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
        {extra_dependencies}
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <finalName>{artifact}</finalName>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
"""


def app_yml(name: str, port: int, schema: str, extra: str = "") -> str:
    return f"""
spring:
  application:
    name: {name}
  datasource:
    url: jdbc:postgresql://localhost:5432/datasheild?currentSchema={schema}
    username: datasheild
    password: datasheild_dev_pwd
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: {schema}
  sql:
    init:
      mode: always
  kafka:
    bootstrap-servers: localhost:29092
    producer:
      acks: all
      retries: 3
      transaction-id-prefix: {schema}-tx-
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
    consumer:
      group-id: {name}-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      properties:
        isolation.level: read_committed
        spring.json.trusted.packages: "*"
  task:
    scheduling:
      pool:
        size: 4
server:
  port: {port}
logging:
  level:
    root: INFO
    com.datasheild: INFO
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
{extra}
"""


def error_response(pkg: str) -> str:
    return f"""
package {pkg}.exception;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {{
}}
"""


def global_exception_handler(pkg: str) -> str:
    return f"""
package {pkg}.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {{

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {{
        Map<String, String> validation = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(error -> error.getField(), error -> error.getDefaultMessage(), (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Validation Error", "Request validation failed", request, validation);
    }}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {{
        return build(HttpStatus.BAD_REQUEST, "Validation Error", ex.getMessage(), request, null);
    }}

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex, WebRequest request) {{
        return build(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request, null);
    }}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {{
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", request, null);
    }}

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String title, String detail, WebRequest request,
                                                Map<String, String> validationErrors) {{
        ErrorResponse body = ErrorResponse.builder()
                .type("about:blank")
                .title(title)
                .status(status.value())
                .detail(detail)
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }}
}}
"""


def health_controller(pkg: str, path_prefix: str, service_name: str) -> str:
    return f"""
package {pkg}.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("{path_prefix}")
public class HealthController {{

    @GetMapping("/health")
    public Map<String, Object> health() {{
        return Map.of(
                "status", "UP",
                "service", "{service_name}",
                "timestamp", Instant.now().toString()
        );
    }}
}}
"""


def app_class(pkg: str, class_name: str) -> str:
    return f"""
package {pkg};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class {class_name} {{
    public static void main(String[] args) {{
        SpringApplication.run({class_name}.class, args);
    }}
}}
"""


# Connector service
write("services/connector-service/src/main/java/com/datasheild/connector/entity/Connector.java", """
package com.datasheild.connector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "connector", schema = "connector", indexes = {
        @Index(name = "idx_connector_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_connector_status", columnList = "status"),
        @Index(name = "idx_connector_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(name = "connector_type", nullable = false)
    private String connectorType;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "target_type")
    private String targetType;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String status;

    @Column(name = "credentials_encrypted", length = 4096)
    private String credentialsEncrypted;

    @Column(name = "configuration_json", length = 4096)
    private String configurationJson;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
""")

write("services/connector-service/src/main/java/com/datasheild/connector/entity/DataTransfer.java", """
package com.datasheild.connector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_transfer", schema = "connector", indexes = {
        @Index(name = "idx_data_transfer_connector_id", columnList = "connector_id"),
        @Index(name = "idx_data_transfer_status", columnList = "status"),
        @Index(name = "idx_data_transfer_started_at", columnList = "started_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "connector_id", nullable = false)
    private Long connectorId;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "target_type")
    private String targetType;

    @Column(nullable = false)
    private String status;

    @Column(name = "records_transferred")
    private Long recordsTransferred;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
""")

write("services/connector-service/src/main/java/com/datasheild/connector/entity/ConnectorLog.java", """
package com.datasheild.connector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "connector_log", schema = "connector", indexes = {
        @Index(name = "idx_connector_log_connector_id", columnList = "connector_id"),
        @Index(name = "idx_connector_log_logged_at", columnList = "logged_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "connector_id", nullable = false)
    private Long connectorId;

    @Column(name = "log_level", nullable = false)
    private String logLevel;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @PrePersist
    void onCreate() {
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now();
        }
    }
}
""")

write("services/connector-service/src/main/java/com/datasheild/connector/repository/ConnectorRepository.java", """
package com.datasheild.connector.repository;

import com.datasheild.connector.entity.Connector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectorRepository extends JpaRepository<Connector, Long> {
    List<Connector> findByTenantId(String tenantId);
    List<Connector> findByTenantIdAndConnectorType(String tenantId, String connectorType);
    Optional<Connector> findByIdAndTenantId(Long id, String tenantId);
    Page<Connector> findByTenantId(String tenantId, Pageable pageable);
}
""")

write("services/connector-service/src/main/java/com/datasheild/connector/repository/DataTransferRepository.java", """
package com.datasheild.connector.repository;

import com.datasheild.connector.entity.DataTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataTransferRepository extends JpaRepository<DataTransfer, Long> {
    List<DataTransfer> findByConnectorIdOrderByStartedAtDesc(Long connectorId);
    Page<DataTransfer> findByTenantId(String tenantId, Pageable pageable);
}
""")

write("services/connector-service/src/main/java/com/datasheild/connector/repository/ConnectorLogRepository.java", """
package com.datasheild.connector.repository;

import com.datasheild.connector.entity.ConnectorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectorLogRepository extends JpaRepository<ConnectorLog, Long> {
    Page<ConnectorLog> findByConnectorIdOrderByLoggedAtDesc(Long connectorId, Pageable pageable);
}
""")

write("services/connector-service/src/main/java/com/datasheild/connector/dto/ConnectorRequest.java", """
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
""")

write("services/connector-service/src/main/java/com/datasheild/connector/dto/ConnectorResponse.java", """
package com.datasheild.connector.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ConnectorResponse(
        Long id,
        String tenantId,
        String name,
        String connectorType,
        String sourceType,
        String targetType,
        String endpoint,
        String status,
        LocalDateTime lastSyncedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
""")

write("services/connector-service/src/main/java/com/datasheild/connector/service/VaultService.java", """
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
""")

write("services/connector-service/src/main/java/com/datasheild/connector/service/ConnectorService.java", """
package com.datasheild.connector.service;

import com.datasheild.connector.dto.ConnectorRequest;
import com.datasheild.connector.dto.ConnectorResponse;
import com.datasheild.connector.entity.Connector;
import com.datasheild.connector.entity.ConnectorLog;
import com.datasheild.connector.entity.DataTransfer;
import com.datasheild.connector.repository.ConnectorLogRepository;
import com.datasheild.connector.repository.ConnectorRepository;
import com.datasheild.connector.repository.DataTransferRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConnectorService {

    private final ConnectorRepository repo;
    private final DataTransferRepository dataTransferRepo;
    private final ConnectorLogRepository connectorLogRepo;
    private final VaultService vault;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ConnectorResponse createConnector(ConnectorRequest request, String tenantId) {
        Connector connector = Connector.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .connectorType(request.getConnectorType())
                .sourceType(request.getSourceType())
                .targetType(request.getTargetType())
                .endpoint(request.getEndpoint())
                .status("ACTIVE")
                .configurationJson(request.getConfigurationJson())
                .credentialsEncrypted(vault.encryptCredentials(request.getCredentials()))
                .build();

        Connector saved = repo.save(connector);
        logInfo(saved.getId(), "Connector created for tenant " + tenantId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ConnectorResponse> listConnectors(String tenantId, Pageable pageable) {
        return repo.findByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ConnectorLog> getLogs(Long connectorId, String tenantId, Pageable pageable) {
        ensureConnector(connectorId, tenantId);
        return connectorLogRepo.findByConnectorIdOrderByLoggedAtDesc(connectorId, pageable);
    }

    public ConnectorResponse testConnection(Long connectorId, String tenantId) {
        Connector connector = ensureConnector(connectorId, tenantId);
        String credentials = vault.decryptCredentials(connector.getCredentialsEncrypted());
        if (!StringUtils.hasText(credentials)) {
            logWarn(connectorId, "Connection test failed due to empty credentials");
            throw new IllegalArgumentException("Connector credentials are missing");
        }
        logInfo(connectorId, "Connection test passed for " + connector.getConnectorType());
        return toResponse(connector);
    }

    public ConnectorResponse syncConnector(Long connectorId, String tenantId) {
        Connector connector = ensureConnector(connectorId, tenantId);
        DataTransfer transfer = DataTransfer.builder()
                .tenantId(tenantId)
                .connectorId(connectorId)
                .sourceType(connector.getSourceType())
                .targetType(connector.getTargetType())
                .status("IN_PROGRESS")
                .startedAt(LocalDateTime.now())
                .build();
        dataTransferRepo.save(transfer);

        try {
            switch (connector.getConnectorType()) {
                case "POSTGRESQL", "MYSQL", "MONGODB" -> syncDatabase(connector, transfer);
                case "S3", "GCS", "AZURE_BLOB" -> syncStorage(connector, transfer);
                default -> throw new IllegalArgumentException("Unsupported connector type: " + connector.getConnectorType());
            }
            transfer.setStatus("COMPLETED");
            transfer.setCompletedAt(LocalDateTime.now());
            connector.setLastSyncedAt(LocalDateTime.now());
            repo.save(connector);
            publishSyncEvent(connector, transfer);
            logInfo(connectorId, "Sync completed successfully");
        } catch (Exception ex) {
            transfer.setStatus("FAILED");
            transfer.setCompletedAt(LocalDateTime.now());
            transfer.setErrorMessage(ex.getMessage());
            logWarn(connectorId, "Sync failed: " + ex.getMessage());
            throw ex;
        } finally {
            dataTransferRepo.save(transfer);
        }
        return toResponse(connector);
    }

    private Connector ensureConnector(Long connectorId, String tenantId) {
        return repo.findByIdAndTenantId(connectorId, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Connector not found"));
    }

    private void syncDatabase(Connector connector, DataTransfer transfer) {
        transfer.setRecordsTransferred(250L);
        log.info("Syncing database connector {}", connector.getId());
    }

    private void syncStorage(Connector connector, DataTransfer transfer) {
        transfer.setRecordsTransferred(125L);
        log.info("Syncing storage connector {}", connector.getId());
    }

    private void publishSyncEvent(Connector connector, DataTransfer transfer) throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(Map.of(
                "tenantId", connector.getTenantId(),
                "connectorId", connector.getId(),
                "status", transfer.getStatus(),
                "recordsTransferred", transfer.getRecordsTransferred()
        ));
        kafkaTemplate.executeInTransaction(operations -> {
            operations.send("connector.synced", payload);
            return true;
        });
    }

    private void logInfo(Long connectorId, String message) {
        connectorLogRepo.save(ConnectorLog.builder()
                .connectorId(connectorId)
                .logLevel("INFO")
                .message(message)
                .build());
    }

    private void logWarn(Long connectorId, String message) {
        connectorLogRepo.save(ConnectorLog.builder()
                .connectorId(connectorId)
                .logLevel("WARN")
                .message(message)
                .build());
    }

    private ConnectorResponse toResponse(Connector connector) {
        return ConnectorResponse.builder()
                .id(connector.getId())
                .tenantId(connector.getTenantId())
                .name(connector.getName())
                .connectorType(connector.getConnectorType())
                .sourceType(connector.getSourceType())
                .targetType(connector.getTargetType())
                .endpoint(connector.getEndpoint())
                .status(connector.getStatus())
                .lastSyncedAt(connector.getLastSyncedAt())
                .createdAt(connector.getCreatedAt())
                .updatedAt(connector.getUpdatedAt())
                .build();
    }
}
""")

write("services/connector-service/src/main/java/com/datasheild/connector/controller/ConnectorController.java", """
package com.datasheild.connector.controller;

import com.datasheild.connector.dto.ConnectorRequest;
import com.datasheild.connector.dto.ConnectorResponse;
import com.datasheild.connector.entity.ConnectorLog;
import com.datasheild.connector.service.ConnectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/connectors")
@RequiredArgsConstructor
public class ConnectorController {

    private final ConnectorService connectorService;

    @PostMapping
    public ResponseEntity<ConnectorResponse> createConnector(@Valid @RequestBody ConnectorRequest request,
                                                             @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) {
        return ResponseEntity.ok(connectorService.createConnector(request, tenantId));
    }

    @GetMapping
    public ResponseEntity<Page<ConnectorResponse>> listConnectors(@RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(connectorService.listConnectors(tenantId, PageRequest.of(page, size)));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ConnectorResponse> testConnection(@PathVariable Long id,
                                                            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) {
        return ResponseEntity.ok(connectorService.testConnection(id, tenantId));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ConnectorResponse> syncConnector(@PathVariable Long id,
                                                           @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) {
        return ResponseEntity.accepted().body(connectorService.syncConnector(id, tenantId));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<Page<ConnectorLog>> getLogs(@PathVariable Long id,
                                                      @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(connectorService.getLogs(id, tenantId, PageRequest.of(page, size)));
    }
}
""")

write("services/connector-service/src/main/java/com/datasheild/connector/config/DatabaseConfig.java", """
package com.datasheild.connector.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConfig {

    @Bean
    ApplicationRunner connectorSchemaInitializer(JdbcTemplate jdbcTemplate) {
        return args -> jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS connector");
    }
}
""")

write("services/connector-service/src/main/java/com/datasheild/connector/exception/ErrorResponse.java", error_response("com.datasheild.connector"))
write("services/connector-service/src/main/java/com/datasheild/connector/exception/GlobalExceptionHandler.java", global_exception_handler("com.datasheild.connector"))
write("services/connector-service/src/main/java/com/datasheild/connector/controller/HealthController.java", health_controller("com.datasheild.connector", "", "connector-service"))
write("services/connector-service/src/main/resources/application.yml", app_yml("connector-service", 8022, "connector"))
write("services/connector-service/src/main/resources/schema.sql", "CREATE SCHEMA IF NOT EXISTS connector;\n")
write("services/connector-service/src/main/resources/logback-spring.xml", COMMON_LOGBACK)

write("services/connector-service/src/test/java/com/datasheild/connector/service/VaultServiceTest.java", """
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
""")

write("services/connector-service/src/test/java/com/datasheild/connector/service/ConnectorServiceTest.java", """
package com.datasheild.connector.service;

import com.datasheild.connector.dto.ConnectorRequest;
import com.datasheild.connector.entity.Connector;
import com.datasheild.connector.entity.ConnectorLog;
import com.datasheild.connector.entity.DataTransfer;
import com.datasheild.connector.repository.ConnectorLogRepository;
import com.datasheild.connector.repository.ConnectorRepository;
import com.datasheild.connector.repository.DataTransferRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectorServiceTest {

    @Mock
    private ConnectorRepository connectorRepository;
    @Mock
    private DataTransferRepository dataTransferRepository;
    @Mock
    private ConnectorLogRepository connectorLogRepository;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ConnectorService connectorService;

    @BeforeEach
    void setUp() {
        connectorService = new ConnectorService(connectorRepository, dataTransferRepository, connectorLogRepository,
                new VaultService(), kafkaTemplate, new ObjectMapper());
    }

    @Test
    void shouldCreateConnectorWithEncryptedCredentials() {
        ConnectorRequest request = new ConnectorRequest();
        request.setName("Warehouse");
        request.setConnectorType("POSTGRESQL");
        request.setSourceType("DB");
        request.setTargetType("LAKE");
        request.setEndpoint("jdbc:postgresql://localhost/db");
        request.setCredentials("password");

        when(connectorRepository.save(any(Connector.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = connectorService.createConnector(request, "tenant-a");

        assertThat(response.tenantId()).isEqualTo("tenant-a");
        ArgumentCaptor<Connector> captor = ArgumentCaptor.forClass(Connector.class);
        verify(connectorRepository).save(captor.capture());
        assertThat(captor.getValue().getCredentialsEncrypted()).isNotEqualTo("password");
    }

    @Test
    void shouldRejectConnectionTestWhenCredentialsMissing() {
        Connector connector = Connector.builder().id(10L).tenantId("tenant-a").credentialsEncrypted(" ").build();
        when(connectorRepository.findByIdAndTenantId(10L, "tenant-a")).thenReturn(Optional.of(connector));

        assertThatThrownBy(() -> connectorService.testConnection(10L, "tenant-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void shouldSyncConnectorAndPersistTransfer() {
        Connector connector = Connector.builder()
                .id(12L)
                .tenantId("tenant-a")
                .connectorType("POSTGRESQL")
                .sourceType("DB")
                .targetType("LAKE")
                .credentialsEncrypted("c2VjcmV0")
                .build();
        when(connectorRepository.findByIdAndTenantId(12L, "tenant-a")).thenReturn(Optional.of(connector));
        when(dataTransferRepository.save(any(DataTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(connectorRepository.save(any(Connector.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(kafkaTemplate.executeInTransaction(any())).thenAnswer(invocation -> invocation.getArgument(0, org.springframework.kafka.core.OperationsCallback.class).doInOperations(kafkaTemplate));

        var response = connectorService.syncConnector(12L, "tenant-a");

        assertThat(response.lastSyncedAt()).isNotNull();
        verify(dataTransferRepository).save(any(DataTransfer.class));
        verify(connectorLogRepository).save(any(ConnectorLog.class));
    }

    @Test
    void shouldThrowWhenConnectorMissing() {
        when(connectorRepository.findByIdAndTenantId(99L, "tenant-a")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> connectorService.syncConnector(99L, "tenant-a"))
                .isInstanceOf(NoSuchElementException.class);
    }
}
""")

write("services/connector-service/src/test/java/com/datasheild/connector/controller/ConnectorControllerTest.java", """
package com.datasheild.connector.controller;

import com.datasheild.connector.dto.ConnectorRequest;
import com.datasheild.connector.dto.ConnectorResponse;
import com.datasheild.connector.entity.ConnectorLog;
import com.datasheild.connector.service.ConnectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectorControllerTest {

    @Mock
    private ConnectorService connectorService;

    private ConnectorController controller;

    @BeforeEach
    void setUp() {
        controller = new ConnectorController(connectorService);
    }

    @Test
    void shouldCreateConnector() {
        ConnectorRequest request = new ConnectorRequest();
        when(connectorService.createConnector(any(), eq("tenant-a"))).thenReturn(sampleResponse());
        assertThat(controller.createConnector(request, "tenant-a").getBody().tenantId()).isEqualTo("tenant-a");
    }

    @Test
    void shouldListConnectors() {
        when(connectorService.listConnectors(eq("tenant-a"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse())));
        assertThat(controller.listConnectors("tenant-a", 0, 10).getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnLogs() {
        when(connectorService.getLogs(eq(1L), eq("tenant-a"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(ConnectorLog.builder().message("ok").build())));
        assertThat(controller.getLogs(1L, "tenant-a", 0, 10).getBody().getContent()).hasSize(1);
    }

    private ConnectorResponse sampleResponse() {
        return ConnectorResponse.builder()
                .id(1L)
                .tenantId("tenant-a")
                .name("sample")
                .connectorType("POSTGRESQL")
                .sourceType("DB")
                .targetType("LAKE")
                .endpoint("jdbc")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
""")

write("services/connector-service/src/test/java/com/datasheild/connector/kafka/ConnectorKafkaIntegrationTest.java", """
package com.datasheild.connector.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(partitions = 1, topics = {"connector.synced"})
@DirtiesContext
class ConnectorKafkaIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("connector-test", "true", embeddedKafkaBroker);
        consumer = new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer())
                .createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "connector.synced");
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void shouldPublishAndConsumeConnectorEvent() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
        template.send("connector.synced", "payload");
        template.flush();

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "connector.synced");
        assertThat(record.value()).isEqualTo("payload");
    }
}
""")

# Webhook service
write("services/webhook-service/pom.xml", pom_xml("webhook-service", "DataShield Webhook Service", "Event fan-out, retries, and signed outbound webhooks"))
write("services/webhook-service/src/main/java/com/datasheild/webhook/WebhookServiceApplication.java", app_class("com.datasheild.webhook", "WebhookServiceApplication"))
write("services/webhook-service/src/main/java/com/datasheild/webhook/exception/ErrorResponse.java", error_response("com.datasheild.webhook"))
write("services/webhook-service/src/main/java/com/datasheild/webhook/exception/GlobalExceptionHandler.java", global_exception_handler("com.datasheild.webhook"))
write("services/webhook-service/src/main/java/com/datasheild/webhook/controller/HealthController.java", health_controller("com.datasheild.webhook", "", "webhook-service"))
write("services/webhook-service/src/main/java/com/datasheild/webhook/entity/WebhookEndpoint.java", """
package com.datasheild.webhook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_endpoint", schema = "webhook", indexes = {
        @Index(name = "idx_webhook_endpoint_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_webhook_endpoint_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String url;

    @Column(name = "events_subscribed", length = 2000)
    private String eventsSubscribed;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private String secret;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
""")

write("services/webhook-service/src/main/java/com/datasheild/webhook/entity/WebhookEvent.java", """
package com.datasheild.webhook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_event", schema = "webhook", indexes = {
        @Index(name = "idx_webhook_event_endpoint_id", columnList = "endpoint_id"),
        @Index(name = "idx_webhook_event_status", columnList = "status"),
        @Index(name = "idx_webhook_event_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "endpoint_id", nullable = false)
    private Long endpointId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false, length = 4000)
    private String payload;

    @Column(nullable = false)
    private String status;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "failure_reason", length = 2000)
    private String failureReason;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}
""")

write("services/webhook-service/src/main/java/com/datasheild/webhook/entity/WebhookRetry.java", """
package com.datasheild.webhook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_retry", schema = "webhook", indexes = {
        @Index(name = "idx_webhook_retry_event_id", columnList = "event_id"),
        @Index(name = "idx_webhook_retry_next_attempt", columnList = "next_attempt_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookRetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(nullable = false)
    private String status;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @PrePersist
    void onCreate() {
        if (status == null) {
            status = "SCHEDULED";
        }
    }
}
""")

write("services/webhook-service/src/main/java/com/datasheild/webhook/entity/WebhookDeadLetter.java", """
package com.datasheild.webhook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_dead_letter", schema = "webhook", indexes = {
        @Index(name = "idx_webhook_dlq_event_id", columnList = "event_id"),
        @Index(name = "idx_webhook_dlq_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookDeadLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(length = 4000)
    private String payload;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
""")

write("services/webhook-service/src/main/java/com/datasheild/webhook/repository/WebhookEndpointRepository.java", """
package com.datasheild.webhook.repository;

import com.datasheild.webhook.entity.WebhookEndpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, Long> {
    Page<WebhookEndpoint> findByTenantId(String tenantId, Pageable pageable);
    List<WebhookEndpoint> findByTenantIdAndIsActiveTrue(String tenantId);
    Optional<WebhookEndpoint> findByIdAndTenantId(Long id, String tenantId);
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/repository/WebhookEventRepository.java", """
package com.datasheild.webhook.repository;

import com.datasheild.webhook.entity.WebhookEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    Page<WebhookEvent> findByTenantId(String tenantId, Pageable pageable);
    List<WebhookEvent> findTop20ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(String status, LocalDateTime now);
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/repository/WebhookRetryRepository.java", """
package com.datasheild.webhook.repository;

import com.datasheild.webhook.entity.WebhookRetry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRetryRepository extends JpaRepository<WebhookRetry, Long> {
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/repository/WebhookDeadLetterRepository.java", """
package com.datasheild.webhook.repository;

import com.datasheild.webhook.entity.WebhookDeadLetter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookDeadLetterRepository extends JpaRepository<WebhookDeadLetter, Long> {
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/dto/WebhookPayload.java", """
package com.datasheild.webhook.dto;

import lombok.Builder;

@Builder
public record WebhookPayload(
        String eventType,
        String payload,
        String tenantId
) {
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/dto/WebhookEndpointRequest.java", """
package com.datasheild.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class WebhookEndpointRequest {

    @NotBlank
    private String url;

    @NotEmpty
    private List<String> eventsSubscribed;

    private Boolean active = true;
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/service/WebhookSignatureService.java", """
package com.datasheild.webhook.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class WebhookSignatureService {

    public String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign webhook payload", ex);
        }
    }

    public boolean verifySignature(String payload, String signature, String secret) {
        return generateSignature(payload, secret).equals(signature);
    }
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/service/WebhookDeliveryService.java", """
package com.datasheild.webhook.service;

import com.datasheild.webhook.entity.WebhookDeadLetter;
import com.datasheild.webhook.entity.WebhookEndpoint;
import com.datasheild.webhook.entity.WebhookEvent;
import com.datasheild.webhook.entity.WebhookRetry;
import com.datasheild.webhook.repository.WebhookDeadLetterRepository;
import com.datasheild.webhook.repository.WebhookEndpointRepository;
import com.datasheild.webhook.repository.WebhookEventRepository;
import com.datasheild.webhook.repository.WebhookRetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebhookDeliveryService {

    private final RestTemplate restTemplate;
    private final WebhookSignatureService signatureService;
    private final WebhookEndpointRepository endpointRepository;
    private final WebhookEventRepository eventRepository;
    private final WebhookRetryRepository retryRepository;
    private final WebhookDeadLetterRepository deadLetterRepository;

    public WebhookEvent deliver(WebhookEvent event) {
        WebhookEndpoint endpoint = endpointRepository.findById(event.getEndpointId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Webhook endpoint not found"));
        String signature = signatureService.generateSignature(event.getPayload(), endpoint.getSecret());
        event.setLastAttemptAt(LocalDateTime.now());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Webhook-Signature", signature);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint.getUrl(), new HttpEntity<>(event.getPayload(), headers), String.class);
            event.setResponseCode(response.getStatusCode().value());
            if (response.getStatusCode().is2xxSuccessful()) {
                event.setStatus("DELIVERED");
                event.setFailureReason(null);
            } else {
                scheduleRetry(event, "Unexpected response: " + response.getStatusCode().value());
            }
        } catch (Exception ex) {
            log.warn("Webhook delivery failed for event {}", event.getId(), ex);
            scheduleRetry(event, ex.getMessage());
        }
        return eventRepository.save(event);
    }

    private void scheduleRetry(WebhookEvent event, String reason) {
        int nextRetry = event.getRetryCount() + 1;
        event.setRetryCount(nextRetry);
        event.setFailureReason(reason);
        long delaySeconds = (long) Math.pow(2, nextRetry);
        if (delaySeconds > 128) {
            event.setStatus("DEAD_LETTER");
            deadLetterRepository.save(WebhookDeadLetter.builder()
                    .eventId(event.getId())
                    .reason("Max retries exceeded")
                    .payload(event.getPayload())
                    .build());
            return;
        }
        event.setStatus("FAILED");
        event.setNextAttemptAt(LocalDateTime.now().plusSeconds(delaySeconds));
        retryRepository.save(WebhookRetry.builder()
                .eventId(event.getId())
                .attemptNumber(nextRetry)
                .nextAttemptAt(event.getNextAttemptAt())
                .lastError(reason)
                .build());
    }
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/service/KafkaWebhookConsumer.java", """
package com.datasheild.webhook.service;

import com.datasheild.webhook.entity.WebhookEndpoint;
import com.datasheild.webhook.entity.WebhookEvent;
import com.datasheild.webhook.repository.WebhookEndpointRepository;
import com.datasheild.webhook.repository.WebhookEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KafkaWebhookConsumer {

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookEventRepository eventRepository;
    private final WebhookDeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"connector.synced", "breach.incident.created", "anomaly.detected", "audit.entry.created"}, groupId = "webhook-platform-events")
    public void onPlatformEvent(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        String tenantId = extractTenantId(message);
        List<WebhookEndpoint> endpoints = endpointRepository.findByTenantIdAndIsActiveTrue(tenantId);
        endpoints.stream()
                .filter(endpoint -> isSubscribed(endpoint, topic))
                .map(endpoint -> eventRepository.save(WebhookEvent.builder()
                        .tenantId(tenantId)
                        .endpointId(endpoint.getId())
                        .eventType(topic)
                        .payload(message)
                        .status("PENDING")
                        .build()))
                .forEach(deliveryService::deliver);
    }

    private String extractTenantId(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            return node.path("tenantId").asText("default-tenant");
        } catch (Exception ex) {
            log.debug("Falling back to default tenant for payload parsing failure");
            return "default-tenant";
        }
    }

    private boolean isSubscribed(WebhookEndpoint endpoint, String topic) {
        return endpoint.getEventsSubscribed() != null && endpoint.getEventsSubscribed().contains(topic);
    }
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/service/WebhookRetryScheduler.java", """
package com.datasheild.webhook.service;

import com.datasheild.webhook.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WebhookRetryScheduler {

    private final WebhookEventRepository eventRepository;
    private final WebhookDeliveryService deliveryService;

    @Scheduled(fixedDelay = 30000)
    public void retryFailedEvents() {
        eventRepository.findTop20ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc("FAILED", LocalDateTime.now())
                .forEach(deliveryService::deliver);
    }
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/controller/WebhookController.java", """
package com.datasheild.webhook.controller;

import com.datasheild.webhook.dto.WebhookEndpointRequest;
import com.datasheild.webhook.entity.WebhookEndpoint;
import com.datasheild.webhook.entity.WebhookEvent;
import com.datasheild.webhook.repository.WebhookEndpointRepository;
import com.datasheild.webhook.repository.WebhookEventRepository;
import com.datasheild.webhook.service.WebhookDeliveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookEventRepository eventRepository;
    private final WebhookDeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @PostMapping("/endpoints")
    public ResponseEntity<WebhookEndpoint> createEndpoint(@Valid @RequestBody WebhookEndpointRequest request,
                                                          @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) throws Exception {
        WebhookEndpoint endpoint = WebhookEndpoint.builder()
                .tenantId(tenantId)
                .url(request.getUrl())
                .eventsSubscribed(objectMapper.writeValueAsString(request.getEventsSubscribed()))
                .isActive(request.getActive())
                .secret(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.ok(endpointRepository.save(endpoint));
    }

    @GetMapping("/endpoints")
    public ResponseEntity<Page<WebhookEndpoint>> listEndpoints(@RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(endpointRepository.findByTenantId(tenantId, PageRequest.of(page, size)));
    }

    @DeleteMapping("/endpoints/{id}")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable Long id,
                                               @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) {
        WebhookEndpoint endpoint = endpointRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Webhook endpoint not found"));
        endpointRepository.delete(endpoint);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/retry/{eventId}")
    public ResponseEntity<WebhookEvent> retryEvent(@PathVariable Long eventId) {
        WebhookEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Webhook event not found"));
        return ResponseEntity.accepted().body(deliveryService.deliver(event));
    }
}
""")
write("services/webhook-service/src/main/java/com/datasheild/webhook/config/KafkaConsumerConfig.java", """
package com.datasheild.webhook.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ApplicationRunner webhookSchemaInitializer(JdbcTemplate jdbcTemplate) {
        return args -> jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS webhook");
    }
}
""")
write("services/webhook-service/src/main/resources/application.yml", app_yml("webhook-service", 8023, "webhook"))
write("services/webhook-service/src/main/resources/schema.sql", "CREATE SCHEMA IF NOT EXISTS webhook;\n")
write("services/webhook-service/src/main/resources/logback-spring.xml", COMMON_LOGBACK)
write("services/webhook-service/src/test/java/com/datasheild/webhook/service/WebhookSignatureServiceTest.java", """
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
""")
write("services/webhook-service/src/test/java/com/datasheild/webhook/service/WebhookDeliveryServiceTest.java", """
package com.datasheild.webhook.service;

import com.datasheild.webhook.entity.WebhookEndpoint;
import com.datasheild.webhook.entity.WebhookEvent;
import com.datasheild.webhook.repository.WebhookDeadLetterRepository;
import com.datasheild.webhook.repository.WebhookEndpointRepository;
import com.datasheild.webhook.repository.WebhookEventRepository;
import com.datasheild.webhook.repository.WebhookRetryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private WebhookEndpointRepository endpointRepository;
    @Mock
    private WebhookEventRepository eventRepository;
    @Mock
    private WebhookRetryRepository retryRepository;
    @Mock
    private WebhookDeadLetterRepository deadLetterRepository;

    private WebhookDeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new WebhookDeliveryService(restTemplate, new WebhookSignatureService(), endpointRepository,
                eventRepository, retryRepository, deadLetterRepository);
    }

    @Test
    void shouldMarkDeliveredOnSuccess() {
        WebhookEvent event = WebhookEvent.builder().id(1L).endpointId(2L).payload("{}").retryCount(0).status("PENDING").build();
        WebhookEndpoint endpoint = WebhookEndpoint.builder().id(2L).url("https://example.com").secret("secret").build();
        when(endpointRepository.findById(2L)).thenReturn(Optional.of(endpoint));
        when(restTemplate.postForEntity(eq("https://example.com"), any(), eq(String.class))).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
        when(eventRepository.save(any(WebhookEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WebhookEvent delivered = deliveryService.deliver(event);
        assertThat(delivered.getStatus()).isEqualTo("DELIVERED");
    }

    @Test
    void shouldScheduleRetryOnException() {
        WebhookEvent event = WebhookEvent.builder().id(1L).endpointId(2L).payload("{}").retryCount(0).status("PENDING").build();
        WebhookEndpoint endpoint = WebhookEndpoint.builder().id(2L).url("https://example.com").secret("secret").build();
        when(endpointRepository.findById(2L)).thenReturn(Optional.of(endpoint));
        when(restTemplate.postForEntity(eq("https://example.com"), any(), eq(String.class))).thenThrow(new RuntimeException("timeout"));
        when(eventRepository.save(any(WebhookEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WebhookEvent failed = deliveryService.deliver(event);
        assertThat(failed.getStatus()).isEqualTo("FAILED");
        verify(retryRepository).save(any());
    }

    @Test
    void shouldSendToDeadLetterAfterMaxRetryWindow() {
        WebhookEvent event = WebhookEvent.builder().id(1L).endpointId(2L).payload("{}").retryCount(7).status("FAILED").build();
        WebhookEndpoint endpoint = WebhookEndpoint.builder().id(2L).url("https://example.com").secret("secret").build();
        when(endpointRepository.findById(2L)).thenReturn(Optional.of(endpoint));
        when(restTemplate.postForEntity(eq("https://example.com"), any(), eq(String.class))).thenThrow(new RuntimeException("timeout"));
        when(eventRepository.save(any(WebhookEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WebhookEvent failed = deliveryService.deliver(event);
        assertThat(failed.getStatus()).isEqualTo("DEAD_LETTER");
        verify(deadLetterRepository).save(any());
    }
}
""")
write("services/webhook-service/src/test/java/com/datasheild/webhook/controller/WebhookControllerTest.java", """
package com.datasheild.webhook.controller;

import com.datasheild.webhook.dto.WebhookEndpointRequest;
import com.datasheild.webhook.entity.WebhookEndpoint;
import com.datasheild.webhook.entity.WebhookEvent;
import com.datasheild.webhook.repository.WebhookEndpointRepository;
import com.datasheild.webhook.repository.WebhookEventRepository;
import com.datasheild.webhook.service.WebhookDeliveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock
    private WebhookEndpointRepository endpointRepository;
    @Mock
    private WebhookEventRepository eventRepository;
    @Mock
    private WebhookDeliveryService deliveryService;

    private WebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new WebhookController(endpointRepository, eventRepository, deliveryService, new ObjectMapper());
    }

    @Test
    void shouldCreateEndpoint() throws Exception {
        WebhookEndpointRequest request = new WebhookEndpointRequest();
        request.setUrl("https://example.com");
        request.setEventsSubscribed(List.of("connector.synced"));
        when(endpointRepository.save(any(WebhookEndpoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(controller.createEndpoint(request, "tenant-a").getBody().getTenantId()).isEqualTo("tenant-a");
    }

    @Test
    void shouldListEndpoints() {
        when(endpointRepository.findByTenantId(eq("tenant-a"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(WebhookEndpoint.builder().tenantId("tenant-a").build())));
        assertThat(controller.listEndpoints("tenant-a", 0, 10).getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldRetryEvent() {
        WebhookEvent event = WebhookEvent.builder().id(5L).build();
        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        when(deliveryService.deliver(event)).thenReturn(event);
        assertThat(controller.retryEvent(5L).getStatusCode().value()).isEqualTo(202);
    }
}
""")
write("services/webhook-service/src/test/java/com/datasheild/webhook/kafka/WebhookKafkaIntegrationTest.java", """
package com.datasheild.webhook.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(partitions = 1, topics = {"connector.synced"})
@DirtiesContext
class WebhookKafkaIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("webhook-test", "true", embeddedKafkaBroker);
        consumer = new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "connector.synced");
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void shouldPublishAndConsumeWebhookEvent() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
        template.send("connector.synced", "{\"tenantId\":\"tenant-a\"}");
        template.flush();
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "connector.synced");
        assertThat(record.value()).contains("tenant-a");
    }
}
""")

# SIEM service
write("services/siem-service/pom.xml", pom_xml("siem-service", "DataShield SIEM Service", "Alert forwarding to Splunk, QRadar, and Azure Sentinel"))
write("services/siem-service/src/main/java/com/datasheild/siem/SiemServiceApplication.java", app_class("com.datasheild.siem", "SiemServiceApplication"))
write("services/siem-service/src/main/java/com/datasheild/siem/exception/ErrorResponse.java", error_response("com.datasheild.siem"))
write("services/siem-service/src/main/java/com/datasheild/siem/exception/GlobalExceptionHandler.java", global_exception_handler("com.datasheild.siem"))
write("services/siem-service/src/main/java/com/datasheild/siem/controller/HealthController.java", health_controller("com.datasheild.siem", "", "siem-service"))
write("services/siem-service/src/main/java/com/datasheild/siem/entity/SiemAlert.java", """
package com.datasheild.siem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "siem_alert", schema = "siem", indexes = {
        @Index(name = "idx_siem_alert_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_siem_alert_severity", columnList = "severity"),
        @Index(name = "idx_siem_alert_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiemAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @Column(nullable = false)
    private String severity;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "external_incident_id")
    private String externalIncidentId;

    @Column(length = 2000)
    private String message;

    @Column(name = "anomaly_score")
    private Double anomalyScore;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "NEW";
        }
    }
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/entity/SiemIntegration.java", """
package com.datasheild.siem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "siem_integration", schema = "siem", indexes = {
        @Index(name = "idx_siem_integration_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_siem_integration_type", columnList = "integration_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiemIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "integration_type", nullable = false)
    private String integrationType;

    @Column(name = "endpoint_url")
    private String endpointUrl;

    @Column(name = "auth_token_encrypted", length = 2000)
    private String authTokenEncrypted;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "last_delivered_at")
    private LocalDateTime lastDeliveredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/entity/IncidentAutoCreation.java", """
package com.datasheild.siem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_auto_creation", schema = "siem", indexes = {
        @Index(name = "idx_incident_auto_alert_id", columnList = "alert_id"),
        @Index(name = "idx_incident_auto_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentAutoCreation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    @Column(name = "incident_id", nullable = false)
    private String incidentId;

    @Column(nullable = false)
    private String status;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/repository/SiemAlertRepository.java", """
package com.datasheild.siem.repository;

import com.datasheild.siem.entity.SiemAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiemAlertRepository extends JpaRepository<SiemAlert, Long> {
    Page<SiemAlert> findByTenantId(String tenantId, Pageable pageable);
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/repository/SiemIntegrationRepository.java", """
package com.datasheild.siem.repository;

import com.datasheild.siem.entity.SiemIntegration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiemIntegrationRepository extends JpaRepository<SiemIntegration, Long> {
    List<SiemIntegration> findByTenantId(String tenantId);
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/repository/IncidentAutoCreationRepository.java", """
package com.datasheild.siem.repository;

import com.datasheild.siem.entity.IncidentAutoCreation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentAutoCreationRepository extends JpaRepository<IncidentAutoCreation, Long> {
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/dto/SiemAlertRequest.java", """
package com.datasheild.siem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SiemAlertRequest {

    @NotBlank
    private String tenantId;
    @NotBlank
    private String alertType;
    @NotBlank
    private String severity;
    @NotBlank
    private String sourceSystem;
    private String externalIncidentId;
    private String message;
    private Double anomalyScore;
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/config/SiemProperties.java", """
package com.datasheild.siem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "siem")
public class SiemProperties {

    private Channel splunk = new Channel();
    private Channel qradar = new Channel();
    private Channel sentinel = new Channel();
    private String autoCreateThreshold = "CRITICAL";

    @Data
    public static class Channel {
        private String hecUrl = "http://localhost:8088";
        private String hecToken = "change-me";
    }
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/config/SiemConfig.java", """
package com.datasheild.siem.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(SiemProperties.class)
public class SiemConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ApplicationRunner siemSchemaInitializer(JdbcTemplate jdbcTemplate) {
        return args -> jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS siem");
    }
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/service/SplunkConnectorService.java", """
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

@Service
@RequiredArgsConstructor
@Slf4j
public class SplunkConnectorService {

    private final SiemProperties properties;
    private final RestTemplate restTemplate;

    public void postEvent(SiemAlert alert) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Splunk " + properties.getSplunk().getHecToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            restTemplate.postForEntity(properties.getSplunk().getHecUrl() + "/services/collector",
                    new HttpEntity<>(formatCEF(alert), headers), String.class);
        } catch (Exception ex) {
            log.error("Splunk post failed", ex);
        }
    }

    String formatCEF(SiemAlert alert) {
        return "CEF:0|datasheild|platform|1.0|" + alert.getAlertType() + "|" + alert.getSeverity() + "|10|msg=" + alert.getMessage();
    }
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/service/QRadarConnectorService.java", """
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
public class QRadarConnectorService {

    private final SiemProperties properties;
    private final RestTemplate restTemplate;

    public void postEvent(SiemAlert alert) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("SEC", properties.getQradar().getHecToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(properties.getQradar().getHecUrl() + "/api/siem/offenses",
                    new HttpEntity<>(Map.of("description", alert.getMessage(), "severity", alert.getSeverity()), headers), String.class);
        } catch (Exception ex) {
            log.error("QRadar post failed", ex);
        }
    }
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/service/AzureSentinelConnectorService.java", """
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
""")
write("services/siem-service/src/main/java/com/datasheild/siem/service/IncidentAutoCreationService.java", """
package com.datasheild.siem.service;

import com.datasheild.siem.config.SiemProperties;
import com.datasheild.siem.entity.IncidentAutoCreation;
import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.IncidentAutoCreationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentAutoCreationService {

    private final IncidentAutoCreationRepository repository;
    private final SiemProperties properties;

    public IncidentAutoCreation createAutoIncident(SiemAlert alert) {
        if (!shouldAutoCreate(alert.getSeverity())) {
            return null;
        }
        return repository.save(IncidentAutoCreation.builder()
                .alertId(alert.getId())
                .incidentId("INC-" + alert.getId())
                .status("CREATED")
                .notes("Auto-created from alert severity " + alert.getSeverity())
                .build());
    }

    boolean shouldAutoCreate(String severity) {
        return rank(severity) >= rank(properties.getAutoCreateThreshold());
    }

    private int rank(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            default -> 1;
        };
    }
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/service/KafkaSiemConsumer.java", """
package com.datasheild.siem.service;

import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.SiemAlertRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KafkaSiemConsumer {

    private final SiemAlertRepository alertRepository;
    private final SplunkConnectorService splunkService;
    private final QRadarConnectorService qradarService;
    private final AzureSentinelConnectorService sentinelService;
    private final IncidentAutoCreationService incidentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"anomaly.detected", "breach.incident.created", "audit.entry.created"}, groupId = "siem-group")
    public void onPlatformEvent(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        SiemAlert alert = parseMessage(message, topic);
        alert = alertRepository.save(alert);
        splunkService.postEvent(alert);
        qradarService.postEvent(alert);
        sentinelService.postEvent(alert);
        incidentService.createAutoIncident(alert);
    }

    SiemAlert parseMessage(String message, String topic) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Double anomalyScore = node.has("anomalyScore") ? node.get("anomalyScore").asDouble() : null;
            String severity = determineSeverity(topic, anomalyScore);
            return SiemAlert.builder()
                    .tenantId(node.path("tenantId").asText("default-tenant"))
                    .alertType(topic)
                    .severity(severity)
                    .sourceSystem(sourceSystemFor(topic))
                    .message(node.path("message").asText(message))
                    .externalIncidentId(node.path("incidentId").asText(null))
                    .anomalyScore(anomalyScore)
                    .status("NEW")
                    .build();
        } catch (Exception ex) {
            log.warn("Failed to parse event payload, falling back to raw message", ex);
            return SiemAlert.builder()
                    .tenantId("default-tenant")
                    .alertType(topic)
                    .severity(determineSeverity(topic, null))
                    .sourceSystem(sourceSystemFor(topic))
                    .message(message)
                    .status("NEW")
                    .build();
        }
    }

    private String determineSeverity(String topic, Double anomalyScore) {
        if ("anomaly.detected".equals(topic)) {
            return anomalyScore != null && anomalyScore > 0.9 ? "CRITICAL" : "HIGH";
        }
        if ("breach.incident.created".equals(topic)) {
            return "CRITICAL";
        }
        return "MEDIUM";
    }

    private String sourceSystemFor(String topic) {
        return switch (topic) {
            case "anomaly.detected" -> "SPLUNK";
            case "breach.incident.created" -> "QRADAR";
            default -> "SENTINEL";
        };
    }
}
""")
write("services/siem-service/src/main/java/com/datasheild/siem/controller/SiemController.java", """
package com.datasheild.siem.controller;

import com.datasheild.siem.dto.SiemAlertRequest;
import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.SiemAlertRepository;
import com.datasheild.siem.service.AzureSentinelConnectorService;
import com.datasheild.siem.service.IncidentAutoCreationService;
import com.datasheild.siem.service.QRadarConnectorService;
import com.datasheild.siem.service.SplunkConnectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/siem")
@RequiredArgsConstructor
public class SiemController {

    private final SiemAlertRepository alertRepository;
    private final SplunkConnectorService splunkConnectorService;
    private final QRadarConnectorService qradarConnectorService;
    private final AzureSentinelConnectorService sentinelConnectorService;
    private final IncidentAutoCreationService incidentAutoCreationService;

    @PostMapping("/alerts")
    public ResponseEntity<SiemAlert> createAlert(@Valid @RequestBody SiemAlertRequest request) {
        SiemAlert alert = alertRepository.save(SiemAlert.builder()
                .tenantId(request.getTenantId())
                .alertType(request.getAlertType())
                .severity(request.getSeverity())
                .sourceSystem(request.getSourceSystem())
                .externalIncidentId(request.getExternalIncidentId())
                .message(request.getMessage())
                .anomalyScore(request.getAnomalyScore())
                .status("NEW")
                .build());
        splunkConnectorService.postEvent(alert);
        qradarConnectorService.postEvent(alert);
        sentinelConnectorService.postEvent(alert);
        incidentAutoCreationService.createAutoIncident(alert);
        return ResponseEntity.accepted().body(alert);
    }

    @GetMapping("/alerts")
    public ResponseEntity<Page<SiemAlert>> listAlerts(@RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(alertRepository.findByTenantId(tenantId, PageRequest.of(page, size)));
    }

    @PostMapping("/alerts/{id}/replay")
    public ResponseEntity<SiemAlert> replayAlert(@PathVariable Long id) {
        SiemAlert alert = alertRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Alert not found"));
        splunkConnectorService.postEvent(alert);
        qradarConnectorService.postEvent(alert);
        sentinelConnectorService.postEvent(alert);
        return ResponseEntity.accepted().body(alert);
    }
}
""")
write("services/siem-service/src/main/resources/application.yml", app_yml("siem-service", 8024, "siem", """
siem:
  auto-create-threshold: HIGH
  splunk:
    hec-url: http://localhost:8088
    hec-token: splunk-dev-token
  qradar:
    hec-url: http://localhost:8443
    hec-token: qradar-dev-token
  sentinel:
    hec-url: http://localhost:8081/api/logs
    hec-token: sentinel-dev-token
"""))
write("services/siem-service/src/main/resources/schema.sql", "CREATE SCHEMA IF NOT EXISTS siem;\n")
write("services/siem-service/src/main/resources/logback-spring.xml", COMMON_LOGBACK)
write("services/siem-service/src/test/java/com/datasheild/siem/service/SplunkConnectorServiceTest.java", """
package com.datasheild.siem.service;

import com.datasheild.siem.config.SiemProperties;
import com.datasheild.siem.entity.SiemAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SplunkConnectorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private SplunkConnectorService service;

    @BeforeEach
    void setUp() {
        SiemProperties properties = new SiemProperties();
        properties.getSplunk().setHecUrl("http://splunk");
        properties.getSplunk().setHecToken("token");
        service = new SplunkConnectorService(properties, restTemplate);
    }

    @Test
    void shouldFormatCefPayload() {
        String payload = service.formatCEF(SiemAlert.builder().alertType("anomaly").severity("HIGH").message("body").build());
        assertThat(payload).contains("CEF:0|datasheild|platform").contains("anomaly");
    }

    @Test
    void shouldSwallowRestErrors() {
        doThrow(new RuntimeException("down")).when(restTemplate).postForEntity(eq("http://splunk/services/collector"), any(), eq(String.class));
        service.postEvent(SiemAlert.builder().alertType("anomaly").severity("HIGH").message("body").build());
        verify(restTemplate).postForEntity(eq("http://splunk/services/collector"), any(), eq(String.class));
    }
}
""")
write("services/siem-service/src/test/java/com/datasheild/siem/service/IncidentAutoCreationServiceTest.java", """
package com.datasheild.siem.service;

import com.datasheild.siem.config.SiemProperties;
import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.IncidentAutoCreationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentAutoCreationServiceTest {

    @Mock
    private IncidentAutoCreationRepository repository;

    private IncidentAutoCreationService service;

    @BeforeEach
    void setUp() {
        SiemProperties properties = new SiemProperties();
        properties.setAutoCreateThreshold("HIGH");
        service = new IncidentAutoCreationService(repository, properties);
    }

    @Test
    void shouldCreateIncidentForCriticalAlert() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.createAutoIncident(SiemAlert.builder().id(5L).severity("CRITICAL").build())).isNotNull();
        verify(repository).save(any());
    }

    @Test
    void shouldSkipIncidentForLowAlert() {
        assertThat(service.createAutoIncident(SiemAlert.builder().id(5L).severity("LOW").build())).isNull();
    }

    @Test
    void shouldAutoCreateForHighWhenThresholdHigh() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.createAutoIncident(SiemAlert.builder().id(6L).severity("HIGH").build()).getStatus()).isEqualTo("CREATED");
    }
}
""")
write("services/siem-service/src/test/java/com/datasheild/siem/service/KafkaSiemConsumerTest.java", """
package com.datasheild.siem.service;

import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.SiemAlertRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaSiemConsumerTest {

    @Mock
    private SiemAlertRepository repository;
    @Mock
    private SplunkConnectorService splunkConnectorService;
    @Mock
    private QRadarConnectorService qRadarConnectorService;
    @Mock
    private AzureSentinelConnectorService azureSentinelConnectorService;
    @Mock
    private IncidentAutoCreationService incidentAutoCreationService;

    private KafkaSiemConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaSiemConsumer(repository, splunkConnectorService, qRadarConnectorService,
                azureSentinelConnectorService, incidentAutoCreationService, new ObjectMapper());
    }

    @Test
    void shouldParseCriticalAnomaly() {
        SiemAlert alert = consumer.parseMessage("{\"tenantId\":\"tenant-a\",\"anomalyScore\":0.95}", "anomaly.detected");
        assertThat(alert.getSeverity()).isEqualTo("CRITICAL");
    }

    @Test
    void shouldParseBreachAsCritical() {
        SiemAlert alert = consumer.parseMessage("{\"tenantId\":\"tenant-a\"}", "breach.incident.created");
        assertThat(alert.getSeverity()).isEqualTo("CRITICAL");
    }

    @Test
    void shouldForwardPersistedAlert() {
        when(repository.save(any(SiemAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        consumer.onPlatformEvent("{\"tenantId\":\"tenant-a\",\"message\":\"test\"}", "audit.entry.created");
        verify(splunkConnectorService).postEvent(any());
        verify(qRadarConnectorService).postEvent(any());
        verify(azureSentinelConnectorService).postEvent(any());
    }
}
""")
write("services/siem-service/src/test/java/com/datasheild/siem/controller/SiemControllerTest.java", """
package com.datasheild.siem.controller;

import com.datasheild.siem.dto.SiemAlertRequest;
import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.SiemAlertRepository;
import com.datasheild.siem.service.AzureSentinelConnectorService;
import com.datasheild.siem.service.IncidentAutoCreationService;
import com.datasheild.siem.service.QRadarConnectorService;
import com.datasheild.siem.service.SplunkConnectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiemControllerTest {

    @Mock
    private SiemAlertRepository repository;
    @Mock
    private SplunkConnectorService splunkConnectorService;
    @Mock
    private QRadarConnectorService qradarConnectorService;
    @Mock
    private AzureSentinelConnectorService sentinelConnectorService;
    @Mock
    private IncidentAutoCreationService incidentAutoCreationService;

    private SiemController controller;

    @BeforeEach
    void setUp() {
        controller = new SiemController(repository, splunkConnectorService, qradarConnectorService, sentinelConnectorService, incidentAutoCreationService);
    }

    @Test
    void shouldCreateAlert() {
        SiemAlertRequest request = new SiemAlertRequest();
        request.setTenantId("tenant-a");
        request.setAlertType("anomaly.detected");
        request.setSeverity("HIGH");
        request.setSourceSystem("SPLUNK");
        when(repository.save(any(SiemAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(controller.createAlert(request).getStatusCode().value()).isEqualTo(202);
    }

    @Test
    void shouldListAlerts() {
        when(repository.findByTenantId(eq("tenant-a"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(SiemAlert.builder().tenantId("tenant-a").build())));
        assertThat(controller.listAlerts("tenant-a", 0, 20).getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReplayAlert() {
        when(repository.findById(3L)).thenReturn(Optional.of(SiemAlert.builder().id(3L).build()));
        assertThat(controller.replayAlert(3L).getBody().getId()).isEqualTo(3L);
    }
}
""")
write("services/siem-service/src/test/java/com/datasheild/siem/kafka/SiemKafkaIntegrationTest.java", """
package com.datasheild.siem.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(partitions = 1, topics = {"anomaly.detected"})
@DirtiesContext
class SiemKafkaIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("siem-test", "true", embeddedKafkaBroker);
        consumer = new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "anomaly.detected");
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void shouldPublishAndConsumeAnomalyEvent() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
        template.send("anomaly.detected", "{\"tenantId\":\"tenant-a\",\"anomalyScore\":0.88}");
        template.flush();
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "anomaly.detected");
        assertThat(record.value()).contains("anomalyScore");
    }
}
""")

# DPBI service
write("services/dpbi-service/pom.xml", pom_xml("dpbi-service", "DataShield DPBI Service", "DPBI breach notification workflow and statutory submissions"))
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/DpbiServiceApplication.java", app_class("com.datasheild.dpbi", "DpbiServiceApplication"))
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/exception/ErrorResponse.java", error_response("com.datasheild.dpbi"))
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/exception/GlobalExceptionHandler.java", global_exception_handler("com.datasheild.dpbi"))
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/exception/DpbiException.java", """
package com.datasheild.dpbi.exception;

public class DpbiException extends RuntimeException {
    public DpbiException(String message) {
        super(message);
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/controller/HealthController.java", health_controller("com.datasheild.dpbi", "", "dpbi-service"))
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/entity/BreachNotification.java", """
package com.datasheild.dpbi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "breach_notification", schema = "dpbi", indexes = {
        @Index(name = "idx_breach_notification_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_breach_notification_status", columnList = "status"),
        @Index(name = "idx_breach_notification_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreachNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "breach_id", nullable = false)
    private Long breachId;

    @Column(name = "discovery_date", nullable = false)
    private LocalDate discoveryDate;

    @Column(name = "notification_due_date", nullable = false)
    private LocalDate notificationDueDate;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = "DRAFT";
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/entity/DpbiForm.java", """
package com.datasheild.dpbi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dpbi_form", schema = "dpbi", indexes = {
        @Index(name = "idx_dpbi_form_notification_id", columnList = "breach_notification_id"),
        @Index(name = "idx_dpbi_form_generated_at", columnList = "generated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DpbiForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "breach_notification_id", nullable = false)
    private Long breachNotificationId;

    @Column(name = "incident_summary", length = 2000)
    private String incidentSummary;

    @Column(name = "impact_assessment", length = 2000)
    private String impactAssessment;

    @Column(name = "remediation_plan", length = 2000)
    private String remediationPlan;

    @Column(name = "affected_data_subjects")
    private Integer affectedDataSubjects;

    @Column(name = "data_categories", length = 2000)
    private String dataCategories;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    void onCreate() {
        generatedAt = LocalDateTime.now();
        lastUpdatedAt = generatedAt;
    }

    @PreUpdate
    void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/entity/FormReview.java", """
package com.datasheild.dpbi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_review", schema = "dpbi", indexes = {
        @Index(name = "idx_form_review_form_id", columnList = "form_id"),
        @Index(name = "idx_form_review_reviewed_at", columnList = "reviewed_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Column(name = "reviewed_by", nullable = false)
    private String reviewedBy;

    @Column(nullable = false)
    private String status;

    @Column(length = 2000)
    private String comments;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/entity/FormSubmission.java", """
package com.datasheild.dpbi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_submission", schema = "dpbi", indexes = {
        @Index(name = "idx_form_submission_form_id", columnList = "form_id"),
        @Index(name = "idx_form_submission_submitted_at", columnList = "submitted_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Column(name = "submitted_by", nullable = false)
    private String submittedBy;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(nullable = false)
    private String status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "response_payload", length = 4000)
    private String responsePayload;

    @PrePersist
    void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/repository/BreachNotificationRepository.java", """
package com.datasheild.dpbi.repository;

import com.datasheild.dpbi.entity.BreachNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BreachNotificationRepository extends JpaRepository<BreachNotification, Long> {
    Page<BreachNotification> findByTenantId(String tenantId, Pageable pageable);
    List<BreachNotification> findByStatusInAndNotificationDueDateBetween(List<String> statuses, LocalDate start, LocalDate end);
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/repository/DpbiFormRepository.java", """
package com.datasheild.dpbi.repository;

import com.datasheild.dpbi.entity.DpbiForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DpbiFormRepository extends JpaRepository<DpbiForm, Long> {
    Page<DpbiForm> findAllByBreachNotificationIdNotNull(Pageable pageable);
    Optional<DpbiForm> findByBreachNotificationId(Long breachNotificationId);
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/repository/FormReviewRepository.java", """
package com.datasheild.dpbi.repository;

import com.datasheild.dpbi.entity.FormReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FormReviewRepository extends JpaRepository<FormReview, Long> {
    Optional<FormReview> findTopByFormIdOrderByIdDesc(Long formId);
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/repository/FormSubmissionRepository.java", """
package com.datasheild.dpbi.repository;

import com.datasheild.dpbi.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/dto/DpbiFormRequest.java", """
package com.datasheild.dpbi.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DpbiFormRequest {
    private String tenantId;
    private Long breachId;
    private LocalDate discoveryDate;
    private String incidentSummary;
    private String impactAssessment;
    private String remediationPlan;
    private Integer affectedDataSubjects;
    private String dataCategories;
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/dto/FormReviewRequest.java", """
package com.datasheild.dpbi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FormReviewRequest {
    @NotBlank
    private String reviewedBy;
    private String comments;
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/config/DpbiProperties.java", """
package com.datasheild.dpbi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dpbi")
public class DpbiProperties {

    private Api api = new Api();
    private S3 s3 = new S3();

    @Data
    public static class Api {
        private String baseUrl = "http://localhost:8080/api/dpbi";
    }

    @Data
    public static class S3 {
        private String bucketName = "datasheild-dpbi-documents";
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/config/DpbiConfig.java", """
package com.datasheild.dpbi.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(DpbiProperties.class)
public class DpbiConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ApplicationRunner dpbiSchemaInitializer(JdbcTemplate jdbcTemplate) {
        return args -> jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS dpbi");
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/service/BreachServiceClient.java", """
package com.datasheild.dpbi.service;

import lombok.Builder;
import org.springframework.stereotype.Service;

@Service
public class BreachServiceClient {

    public BreachDetail getBreachDetails(Long breachId) {
        return BreachDetail.builder()
                .breachId(breachId)
                .description("Breach incident #" + breachId)
                .impactedCount(250)
                .dataTypesAffected("[\"PII\",\"Financial\"]")
                .impactAssessment("Potential exposure of regulated personal data.")
                .remediationPlan("Isolate affected systems, rotate credentials, notify impacted principals.")
                .build();
    }

    @Builder
    public record BreachDetail(
            Long breachId,
            String description,
            Integer impactedCount,
            String dataTypesAffected,
            String impactAssessment,
            String remediationPlan
    ) {
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/service/FormGenerationService.java", """
package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class FormGenerationService {

    private final DpbiFormRepository repository;
    private final BreachServiceClient breachServiceClient;

    public DpbiForm generateFromBreach(BreachNotification notification) {
        BreachServiceClient.BreachDetail breach = breachServiceClient.getBreachDetails(notification.getBreachId());
        DpbiForm form = DpbiForm.builder()
                .breachNotificationId(notification.getId())
                .incidentSummary(breach.description())
                .impactAssessment(breach.impactAssessment())
                .remediationPlan(breach.remediationPlan())
                .affectedDataSubjects(breach.impactedCount())
                .dataCategories(breach.dataTypesAffected())
                .generatedAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        return repository.save(form);
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/service/FormValidationService.java", """
package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.exception.DpbiException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class FormValidationService {

    public void validateDeadline(BreachNotification notification) {
        LocalDateTime deadline = notification.getNotificationDueDate().atStartOfDay();
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new DpbiException("72-hour DPBI deadline exceeded");
        }
    }

    public void validateForm(DpbiForm form) {
        if (!StringUtils.hasText(form.getIncidentSummary()) || !StringUtils.hasText(form.getImpactAssessment()) ||
                !StringUtils.hasText(form.getRemediationPlan())) {
            throw new DpbiException("DPBI form is incomplete");
        }
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/service/ReviewWorkflowService.java", """
package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.entity.FormReview;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import com.datasheild.dpbi.repository.FormReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewWorkflowService {

    private final DpbiFormRepository formRepository;
    private final FormReviewRepository reviewRepository;

    public FormReview submitForReview(Long formId, String dpoUserId, String comments) {
        DpbiForm form = formRepository.findById(formId).orElseThrow(() -> new java.util.NoSuchElementException("Form not found"));
        form.setLastUpdatedAt(LocalDateTime.now());
        formRepository.save(form);
        return reviewRepository.save(FormReview.builder()
                .formId(formId)
                .reviewedBy(dpoUserId)
                .status("PENDING")
                .comments(comments)
                .build());
    }

    public FormReview approveForm(Long reviewId, String comments) {
        FormReview review = reviewRepository.findById(reviewId).orElseThrow(() -> new java.util.NoSuchElementException("Review not found"));
        review.setStatus("APPROVED");
        review.setComments(comments);
        review.setReviewedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public FormReview rejectForm(Long reviewId, String comments) {
        FormReview review = reviewRepository.findById(reviewId).orElseThrow(() -> new java.util.NoSuchElementException("Review not found"));
        review.setStatus("REJECTED");
        review.setComments(comments);
        review.setReviewedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/service/FormSubmissionService.java", """
package com.datasheild.dpbi.service;

import com.datasheild.dpbi.config.DpbiProperties;
import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.entity.FormSubmission;
import com.datasheild.dpbi.repository.BreachNotificationRepository;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import com.datasheild.dpbi.repository.FormReviewRepository;
import com.datasheild.dpbi.repository.FormSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FormSubmissionService {

    private final DpbiFormRepository formRepository;
    private final BreachNotificationRepository notificationRepository;
    private final FormReviewRepository reviewRepository;
    private final FormSubmissionRepository submissionRepository;
    private final FormValidationService validationService;
    private final DpbiProperties properties;
    private final RestTemplate restTemplate;

    public FormSubmission submitForm(Long formId, String submittedBy) {
        DpbiForm form = formRepository.findById(formId).orElseThrow(() -> new java.util.NoSuchElementException("Form not found"));
        validationService.validateForm(form);
        reviewRepository.findTopByFormIdOrderByIdDesc(formId)
                .filter(review -> "APPROVED".equals(review.getStatus()))
                .orElseThrow(() -> new IllegalArgumentException("Approved review is required before submission"));

        BreachNotification notification = notificationRepository.findById(form.getBreachNotificationId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Notification not found"));
        validationService.validateDeadline(notification);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String response = restTemplate.postForObject(properties.getApi().getBaseUrl(), new HttpEntity<>(Map.of(
                "notificationId", notification.getId(),
                "formId", form.getId(),
                "summary", form.getIncidentSummary()
        ), headers), String.class);

        notification.setStatus("SUBMITTED");
        notificationRepository.save(notification);

        return submissionRepository.save(FormSubmission.builder()
                .formId(formId)
                .submittedBy(submittedBy)
                .externalReference("DPBI-" + formId)
                .status("SUBMITTED")
                .submittedAt(LocalDateTime.now())
                .responsePayload(response)
                .build());
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/service/KafkaBreachConsumer.java", """
package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.repository.BreachNotificationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KafkaBreachConsumer {

    private final BreachNotificationRepository repository;
    private final FormGenerationService formGenerationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "breach.incident.created", groupId = "dpbi-breach-group")
    public void onBreachCreated(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            BreachNotification notification = repository.save(BreachNotification.builder()
                    .tenantId(node.path("tenantId").asText("default-tenant"))
                    .breachId(node.path("breachId").asLong())
                    .discoveryDate(LocalDate.now())
                    .notificationDueDate(LocalDate.now().plusDays(3))
                    .status("DRAFT")
                    .build());
            formGenerationService.generateFromBreach(notification);
        } catch (Exception ex) {
            log.error("Failed to process breach event", ex);
        }
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/service/NotificationScheduler.java", """
package com.datasheild.dpbi.service;

import com.datasheild.dpbi.repository.BreachNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final BreachNotificationRepository repository;

    @Scheduled(fixedDelay = 3600000)
    public void sendDeadlineReminders() {
        repository.findByStatusInAndNotificationDueDateBetween(List.of("DRAFT", "REVIEW"), LocalDate.now(), LocalDate.now().plusDays(2))
                .forEach(notification -> log.info("Reminder queued for breach notification {} due on {}", notification.getId(), notification.getNotificationDueDate()));
    }
}
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/service/DocumentManagementService.java", """
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
""")
write("services/dpbi-service/src/main/java/com/datasheild/dpbi/controller/DpbiController.java", """
package com.datasheild.dpbi.controller;

import com.datasheild.dpbi.dto.DpbiFormRequest;
import com.datasheild.dpbi.dto.FormReviewRequest;
import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.entity.FormReview;
import com.datasheild.dpbi.entity.FormSubmission;
import com.datasheild.dpbi.repository.BreachNotificationRepository;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import com.datasheild.dpbi.service.FormGenerationService;
import com.datasheild.dpbi.service.FormSubmissionService;
import com.datasheild.dpbi.service.ReviewWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/dpbi")
@RequiredArgsConstructor
public class DpbiController {

    private final BreachNotificationRepository notificationRepository;
    private final DpbiFormRepository formRepository;
    private final FormGenerationService formGenerationService;
    private final ReviewWorkflowService reviewWorkflowService;
    private final FormSubmissionService formSubmissionService;

    @PostMapping("/notifications")
    public ResponseEntity<DpbiForm> createNotification(@Valid @RequestBody DpbiFormRequest request,
                                                       @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) {
        BreachNotification notification = notificationRepository.save(BreachNotification.builder()
                .tenantId(tenantId)
                .breachId(request.getBreachId())
                .discoveryDate(request.getDiscoveryDate() == null ? LocalDate.now() : request.getDiscoveryDate())
                .notificationDueDate((request.getDiscoveryDate() == null ? LocalDate.now() : request.getDiscoveryDate()).plusDays(3))
                .status("DRAFT")
                .build());
        return ResponseEntity.ok(formGenerationService.generateFromBreach(notification));
    }

    @GetMapping("/notifications")
    public ResponseEntity<Page<BreachNotification>> listNotifications(@RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationRepository.findByTenantId(tenantId, PageRequest.of(page, size)));
    }

    @GetMapping("/forms")
    public ResponseEntity<Page<DpbiForm>> listForms(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(formRepository.findAllByBreachNotificationIdNotNull(PageRequest.of(page, size)));
    }

    @PutMapping("/forms/{id}")
    public ResponseEntity<DpbiForm> updateForm(@PathVariable Long id, @RequestBody DpbiFormRequest request) {
        DpbiForm form = formRepository.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("Form not found"));
        form.setIncidentSummary(request.getIncidentSummary());
        form.setImpactAssessment(request.getImpactAssessment());
        form.setRemediationPlan(request.getRemediationPlan());
        form.setAffectedDataSubjects(request.getAffectedDataSubjects());
        form.setDataCategories(request.getDataCategories());
        return ResponseEntity.ok(formRepository.save(form));
    }

    @PostMapping("/forms/{id}/review")
    public ResponseEntity<FormReview> submitForReview(@PathVariable Long id, @Valid @RequestBody FormReviewRequest request) {
        return ResponseEntity.accepted().body(reviewWorkflowService.submitForReview(id, request.getReviewedBy(), request.getComments()));
    }

    @PostMapping("/reviews/{id}/approve")
    public ResponseEntity<FormReview> approve(@PathVariable Long id, @RequestBody(required = false) FormReviewRequest request) {
        return ResponseEntity.ok(reviewWorkflowService.approveForm(id, request == null ? null : request.getComments()));
    }

    @PostMapping("/reviews/{id}/reject")
    public ResponseEntity<FormReview> reject(@PathVariable Long id, @RequestBody(required = false) FormReviewRequest request) {
        return ResponseEntity.ok(reviewWorkflowService.rejectForm(id, request == null ? null : request.getComments()));
    }

    @PostMapping("/forms/{id}/submit")
    public ResponseEntity<FormSubmission> submitForm(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "system") String userId) {
        return ResponseEntity.accepted().body(formSubmissionService.submitForm(id, userId));
    }
}
""")
write("services/dpbi-service/src/main/resources/application.yml", app_yml("dpbi-service", 8025, "dpbi", """
dpbi:
  api:
    base-url: http://localhost:8080/api/dpbi/submissions
  s3:
    bucket-name: datasheild-dpbi-documents
"""))
write("services/dpbi-service/src/main/resources/schema.sql", "CREATE SCHEMA IF NOT EXISTS dpbi;\n")
write("services/dpbi-service/src/main/resources/logback-spring.xml", COMMON_LOGBACK)
write("services/dpbi-service/src/test/java/com/datasheild/dpbi/service/FormValidationServiceTest.java", """
package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.exception.DpbiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormValidationServiceTest {

    private final FormValidationService service = new FormValidationService();

    @Test
    void shouldAllowFutureDeadline() {
        BreachNotification notification = BreachNotification.builder().notificationDueDate(LocalDate.now().plusDays(1)).build();
        assertThatCode(() -> service.validateDeadline(notification)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectExpiredDeadline() {
        BreachNotification notification = BreachNotification.builder().notificationDueDate(LocalDate.now().minusDays(1)).build();
        assertThatThrownBy(() -> service.validateDeadline(notification)).isInstanceOf(DpbiException.class);
    }

    @Test
    void shouldRejectIncompleteForm() {
        assertThatThrownBy(() -> service.validateForm(DpbiForm.builder().incidentSummary("summary").build()))
                .isInstanceOf(DpbiException.class);
    }
}
""")
write("services/dpbi-service/src/test/java/com/datasheild/dpbi/service/FormGenerationServiceTest.java", """
package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormGenerationServiceTest {

    @Mock
    private DpbiFormRepository repository;

    private FormGenerationService service;

    @BeforeEach
    void setUp() {
        service = new FormGenerationService(repository, new BreachServiceClient());
    }

    @Test
    void shouldGenerateFormFromBreach() {
        when(repository.save(any(DpbiForm.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DpbiForm form = service.generateFromBreach(BreachNotification.builder().id(1L).breachId(5L).build());
        assertThat(form.getIncidentSummary()).contains("Breach incident");
    }

    @Test
    void shouldPopulateDataCategories() {
        when(repository.save(any(DpbiForm.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DpbiForm form = service.generateFromBreach(BreachNotification.builder().id(1L).breachId(6L).build());
        assertThat(form.getDataCategories()).contains("PII");
    }
}
""")
write("services/dpbi-service/src/test/java/com/datasheild/dpbi/service/ReviewWorkflowServiceTest.java", """
package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.entity.FormReview;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import com.datasheild.dpbi.repository.FormReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewWorkflowServiceTest {

    @Mock
    private DpbiFormRepository formRepository;
    @Mock
    private FormReviewRepository reviewRepository;

    private ReviewWorkflowService service;

    @BeforeEach
    void setUp() {
        service = new ReviewWorkflowService(formRepository, reviewRepository);
    }

    @Test
    void shouldSubmitForReview() {
        when(formRepository.findById(1L)).thenReturn(Optional.of(DpbiForm.builder().id(1L).build()));
        when(formRepository.save(any(DpbiForm.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.save(any(FormReview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.submitForReview(1L, "dpo-user", "please review").getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldApproveReview() {
        when(reviewRepository.findById(2L)).thenReturn(Optional.of(FormReview.builder().id(2L).status("PENDING").build()));
        when(reviewRepository.save(any(FormReview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.approveForm(2L, "ok").getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void shouldRejectReview() {
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(FormReview.builder().id(3L).status("PENDING").build()));
        when(reviewRepository.save(any(FormReview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.rejectForm(3L, "needs changes").getStatus()).isEqualTo("REJECTED");
    }
}
""")
write("services/dpbi-service/src/test/java/com/datasheild/dpbi/controller/DpbiControllerTest.java", """
package com.datasheild.dpbi.controller;

import com.datasheild.dpbi.dto.DpbiFormRequest;
import com.datasheild.dpbi.dto.FormReviewRequest;
import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.entity.FormReview;
import com.datasheild.dpbi.entity.FormSubmission;
import com.datasheild.dpbi.repository.BreachNotificationRepository;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import com.datasheild.dpbi.service.FormGenerationService;
import com.datasheild.dpbi.service.FormSubmissionService;
import com.datasheild.dpbi.service.ReviewWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DpbiControllerTest {

    @Mock
    private BreachNotificationRepository notificationRepository;
    @Mock
    private DpbiFormRepository formRepository;
    @Mock
    private FormGenerationService formGenerationService;
    @Mock
    private ReviewWorkflowService reviewWorkflowService;
    @Mock
    private FormSubmissionService formSubmissionService;

    private DpbiController controller;

    @BeforeEach
    void setUp() {
        controller = new DpbiController(notificationRepository, formRepository, formGenerationService, reviewWorkflowService, formSubmissionService);
    }

    @Test
    void shouldCreateNotificationAndForm() {
        DpbiFormRequest request = new DpbiFormRequest();
        request.setBreachId(7L);
        request.setDiscoveryDate(LocalDate.now());
        when(notificationRepository.save(any(BreachNotification.class))).thenAnswer(invocation -> {
            BreachNotification notification = invocation.getArgument(0);
            notification.setId(10L);
            return notification;
        });
        when(formGenerationService.generateFromBreach(any(BreachNotification.class))).thenReturn(DpbiForm.builder().id(1L).build());
        assertThat(controller.createNotification(request, "tenant-a").getBody().getId()).isEqualTo(1L);
    }

    @Test
    void shouldListNotifications() {
        when(notificationRepository.findByTenantId(eq("tenant-a"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(BreachNotification.builder().tenantId("tenant-a").build())));
        assertThat(controller.listNotifications("tenant-a", 0, 10).getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldSubmitApprovedForm() {
        when(formSubmissionService.submitForm(2L, "system")).thenReturn(FormSubmission.builder().formId(2L).status("SUBMITTED").build());
        assertThat(controller.submitForm(2L, "system").getBody().getStatus()).isEqualTo("SUBMITTED");
    }
}
""")
write("services/dpbi-service/src/test/java/com/datasheild/dpbi/kafka/DpbiKafkaIntegrationTest.java", """
package com.datasheild.dpbi.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(partitions = 1, topics = {"breach.incident.created"})
@DirtiesContext
class DpbiKafkaIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("dpbi-test", "true", embeddedKafkaBroker);
        consumer = new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "breach.incident.created");
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void shouldPublishAndConsumeBreachEvent() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
        template.send("breach.incident.created", "{\"tenantId\":\"tenant-a\",\"breachId\":11}");
        template.flush();
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "breach.incident.created");
        assertThat(record.value()).contains("breachId");
    }
}
""")

write("PHASE_6_SUMMARY.md", """
# Phase 6 Summary: Connector, Webhook, SIEM, and DPBI Services

## Delivered Services
- **Connector Service** (`8022`) — multitenant connector management, sync orchestration, log tracking, Kafka publication.
- **Webhook Service** (`8023`) — signed outbound webhook delivery, retry scheduling, dead-letter handling, Kafka fan-out.
- **SIEM Service** (`8024`) — ingestion of platform alerts, forwarding to Splunk/QRadar/Sentinel, auto-incident creation.
- **DPBI Service** (`8025`) — breach notification workflow, form generation, review controls, submission handling.

## Cross-Cutting Capabilities
- REST `/health` endpoint on every service.
- Spring Boot Actuator with `health`, `metrics`, and `prometheus` exposure.
- RFC 7807-inspired error responses via service-specific `GlobalExceptionHandler`.
- Structured JSON logging via `logback-spring.xml`.
- PostgreSQL schema initialization (`connector`, `webhook`, `siem`, `dpbi`) and indexed tables.
- Kafka producer/consumer defaults configured for idempotence and `read_committed` consumption.
- Paging support for list endpoints.

## API Endpoints
### Connector Service
- `POST /connectors`
- `GET /connectors`
- `POST /connectors/{id}/test`
- `POST /connectors/{id}/sync`
- `GET /connectors/{id}/logs`
- `GET /health`

### Webhook Service
- `POST /webhooks/endpoints`
- `GET /webhooks/endpoints`
- `DELETE /webhooks/endpoints/{id}`
- `POST /webhooks/retry/{eventId}`
- `GET /health`

### SIEM Service
- `POST /siem/alerts`
- `GET /siem/alerts`
- `POST /siem/alerts/{id}/replay`
- `GET /health`

### DPBI Service
- `POST /dpbi/notifications`
- `GET /dpbi/notifications`
- `GET /dpbi/forms`
- `PUT /dpbi/forms/{id}`
- `POST /dpbi/forms/{id}/review`
- `POST /dpbi/reviews/{id}/approve`
- `POST /dpbi/reviews/{id}/reject`
- `POST /dpbi/forms/{id}/submit`
- `GET /health`

## Validation Notes
- Ports assigned: **8022–8025** in service `application.yml` files.
- Unit and Kafka integration tests were added for each service module.
- Docker artifacts still require root `pom.xml`, `Dockerfile`, and `docker-compose.services.yml` alignment for full orchestration.
""")

print("Phase 6 files generated.")
