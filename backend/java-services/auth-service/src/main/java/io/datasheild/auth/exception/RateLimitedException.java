package io.datasheild.auth.exception;

public class RateLimitedException extends ApiException {
    public RateLimitedException(String message) {
        super(message, 429);
    }
}
