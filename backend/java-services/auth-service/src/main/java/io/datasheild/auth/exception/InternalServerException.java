package io.datasheild.auth.exception;

public class InternalServerException extends ApiException {
    public InternalServerException(String message) {
        super(message, 500);
    }
    
    public InternalServerException(String message, Throwable cause) {
        super(message, cause, 500);
    }
}
