package io.datasheild.tenantservice.service;

import io.datasheild.tenantservice.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemaProvisioningService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void provisionSchema(String schemaName) {
        log.info("Provisioning schema: {}", schemaName);

        // Create schema
        String createSchemaSQL = String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName);
        jdbcTemplate.execute(createSchemaSQL);
        log.debug("Schema created: {}", schemaName);

        // Create base tables for all services
        createTenantBaseTable(schemaName);
        createAuditTable(schemaName);
        createConfigTable(schemaName);
    }

    @Transactional
    public void seedInitialData(String schemaName, Tenant.TenantTier tier) {
        log.info("Seeding initial data for schema: {} (tier: {})", schemaName, tier);

        // Seed tier-based configuration
        String insertConfigSQL = String.format(
            "INSERT INTO %s.tenant_config (key, value, tier) VALUES (?, ?, ?)",
            schemaName
        );

        switch (tier) {
            case STARTER:
                jdbcTemplate.update(insertConfigSQL, "max_api_concurrent", "10", "STARTER");
                jdbcTemplate.update(insertConfigSQL, "data_retention_days", "30", "STARTER");
                break;
            case PROFESSIONAL:
                jdbcTemplate.update(insertConfigSQL, "max_api_concurrent", "50", "PROFESSIONAL");
                jdbcTemplate.update(insertConfigSQL, "data_retention_days", "90", "PROFESSIONAL");
                break;
            case ENTERPRISE:
                jdbcTemplate.update(insertConfigSQL, "max_api_concurrent", "200", "ENTERPRISE");
                jdbcTemplate.update(insertConfigSQL, "data_retention_days", "365", "ENTERPRISE");
                break;
            case GOVERNMENT:
                jdbcTemplate.update(insertConfigSQL, "max_api_concurrent", "500", "GOVERNMENT");
                jdbcTemplate.update(insertConfigSQL, "data_retention_days", "2555", "GOVERNMENT");
                break;
        }

        log.info("Initial data seeded for schema: {}", schemaName);
    }

    private void createTenantBaseTable(String schemaName) {
        String createTableSQL = String.format(
            "CREATE TABLE IF NOT EXISTS %s.tenant_config (\n" +
            "  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),\n" +
            "  key VARCHAR(255) NOT NULL,\n" +
            "  value TEXT,\n" +
            "  tier VARCHAR(50),\n" +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
            "  UNIQUE(key, tier)\n" +
            ")", schemaName
        );
        jdbcTemplate.execute(createTableSQL);
        log.debug("Table tenant_config created in schema: {}", schemaName);
    }

    private void createAuditTable(String schemaName) {
        String createTableSQL = String.format(
            "CREATE TABLE IF NOT EXISTS %s.audit_trail (\n" +
            "  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),\n" +
            "  entity_type VARCHAR(100) NOT NULL,\n" +
            "  entity_id uuid NOT NULL,\n" +
            "  action VARCHAR(50) NOT NULL,\n" +
            "  actor_id uuid,\n" +
            "  actor_type VARCHAR(50),\n" +
            "  changes jsonb,\n" +
            "  ip_address VARCHAR(45),\n" +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
            ")", schemaName
        );
        jdbcTemplate.execute(createTableSQL);

        String createIndexSQL = String.format(
            "CREATE INDEX IF NOT EXISTS idx_%s_audit_entity ON %s.audit_trail(entity_type, entity_id)",
            schemaName, schemaName
        );
        jdbcTemplate.execute(createIndexSQL);

        log.debug("Table audit_trail created in schema: {}", schemaName);
    }

    private void createConfigTable(String schemaName) {
        String createTableSQL = String.format(
            "CREATE TABLE IF NOT EXISTS %s.feature_toggles (\n" +
            "  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),\n" +
            "  feature_name VARCHAR(255) NOT NULL UNIQUE,\n" +
            "  is_enabled BOOLEAN DEFAULT false,\n" +
            "  description TEXT,\n" +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
            ")", schemaName
        );
        jdbcTemplate.execute(createTableSQL);
        log.debug("Table feature_toggles created in schema: {}", schemaName);
    }
}
