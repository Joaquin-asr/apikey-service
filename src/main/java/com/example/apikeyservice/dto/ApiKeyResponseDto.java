package com.example.apikeyservice.dto;

public record ApiKeyResponseDto(boolean success, String apiKey, String message, String code) {

    public static ApiKeyResponseDto success(String apiKey, String message, String code) {
        return new ApiKeyResponseDto(true, apiKey, message, code);
    }

    public static ApiKeyResponseDto failure(String message, String code) {
        return new ApiKeyResponseDto(false, null, message, code);
    }
}
