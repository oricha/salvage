package com.cardealer.exception;

public class LocalizationException extends RuntimeException {

    public LocalizationException(String message) {
        super(message);
    }

    public LocalizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
