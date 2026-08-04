package io.datasheild.common.error;

import lombok.Getter;

/**
 * Base exception for all DataShield services
 */
@Getter
public class DataShieldException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;
    
    public DataShieldException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public DataShieldException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public static class NotFoundException extends DataShieldException {
        public NotFoundException(String resource, String identifier) {
            super("NOT_FOUND", resource + " not found: " + identifier, 404);
        }
    }
    
    public static class ConflictException extends DataShieldException {
        public ConflictException(String resource, String reason) {
            super("CONFLICT", resource + " conflict: " + reason, 409);
        }
    }
    
    public static class BadRequestException extends DataShieldException {
        public BadRequestException(String message) {
            super("BAD_REQUEST", message, 400);
        }
    }
    
    public static class UnauthorizedException extends DataShieldException {
        public UnauthorizedException(String message) {
            super("UNAUTHORIZED", message, 401);
        }
    }
    
    public static class ForbiddenException extends DataShieldException {
        public ForbiddenException(String message) {
            super("FORBIDDEN", message, 403);
        }
    }
    
    public static class InternalServerException extends DataShieldException {
        public InternalServerException(String message, Throwable cause) {
            super("INTERNAL_SERVER_ERROR", message, 500, cause);
        }
    }
}
