package io.datasheild.auth.exception;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(message, 401);
    }
}
