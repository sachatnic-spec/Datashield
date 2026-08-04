package io.datasheild.auth.exception;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message, 400);
    }
}
