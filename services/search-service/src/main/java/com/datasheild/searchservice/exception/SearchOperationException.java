package com.datasheild.searchservice.exception;

public class SearchOperationException extends RuntimeException {

    public SearchOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchOperationException(String message) {
        super(message);
    }
}
