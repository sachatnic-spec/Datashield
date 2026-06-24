package io.datasheild.auth.exception;

public abstract class ApiException extends RuntimeException {
    private final int statusCode;

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(message, 401);
    }
}

class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(message, 403);
    }
}

class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(message, 404);
    }
}

class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message, 409);
    }
}

class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message, 400);
    }
}

class RateLimitedException extends ApiException {
    public RateLimitedException(String message) {
        super(message, 429);
    }
}

class InternalServerException extends ApiException {
    public InternalServerException(String message) {
        super(message, 500);
    }
    
    public InternalServerException(String message, Throwable cause) {
        super(message, cause, 500);
    }
}
