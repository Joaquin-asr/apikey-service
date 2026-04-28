package com.example.apikeyservice.service;

import com.example.apikeyservice.dto.ApiKeyResponseDto;

public interface ApiKeyService {

    ApiKeyResponseDto createOrGetApiKey(String clientId);

    ApiKeyResponseDto forceRenewApiKey(String renewedApiKeyId, String clientId);

    ApiKeyResponseDto validateApiKey(String clientId, String apiKey);
}
