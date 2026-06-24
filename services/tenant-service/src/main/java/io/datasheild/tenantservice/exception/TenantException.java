package io.datasheild.tenantservice.exception;

import lombok.Getter;

@Getter
public class TenantException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public TenantException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public static class TenantNotFoundException extends TenantException {
        public TenantNotFoundException(String identifier) {
            super("TENANT_NOT_FOUND", "Tenant not found: " + identifier, 404);
        }
    }

    public static class SchemaAlreadyExistsException extends TenantException {
        public SchemaAlreadyExistsException(String schemaName) {
            super("SCHEMA_ALREADY_EXISTS", "Schema already exists: " + schemaName, 409);
        }
    }

    public static class TenantAlreadyExistsException extends TenantException {
        public TenantAlreadyExistsException(String name) {
            super("TENANT_ALREADY_EXISTS", "Tenant already exists: " + name, 409);
        }
    }

    public static class ProvisioningException extends TenantException {
        public ProvisioningException(String tenantId, String reason) {
            super("PROVISIONING_FAILED", "Provisioning failed for " + tenantId + ": " + reason, 500);
        }
    }

    public static class InvalidTenantStateException extends TenantException {
        public InvalidTenantStateException(String message) {
            super("INVALID_TENANT_STATE", message, 400);
        }
    }

    public static class FeatureFlagNotFoundException extends TenantException {
        public FeatureFlagNotFoundException(String flagName) {
            super("FEATURE_FLAG_NOT_FOUND", "Feature flag not found: " + flagName, 404);
        }
    }
}
