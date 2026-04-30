package com.example.apikeyservice.exception;

public class ApiKeyValidationException extends RuntimeException {

    private final String code;

    public ApiKeyValidationException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
