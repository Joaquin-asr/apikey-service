package com.example.apikeyservice.mapper;

import com.example.apikeyservice.entity.ApiKey;
import com.example.apikeyservice.utils.ExpirationCalculatorUtils;
import com.example.apikeyservice.utils.HashUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ApiKeyMapper {

    private final ExpirationCalculatorUtils expirationCalculator;

    public ApiKeyMapper(ExpirationCalculatorUtils expirationCalculator) {
        this.expirationCalculator = expirationCalculator;
    }

    public String generateRawApiKey() {
        return UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
    }

    public ApiKey toEntity(String clientHash, String rawApiKey, String encryptedApiKey) {
        LocalDateTime now = LocalDateTime.now();
        ApiKey entity = new ApiKey();
        entity.setClientId(clientHash);
        entity.setApiKey(encryptedApiKey);
        entity.setApiKeyHash(HashUtils.sha256Hex(rawApiKey));
        entity.setExpirationAt(expirationCalculator.calculateNextExpiration(now));
        entity.setActive("1");
        entity.setRenewCooldownUntil(now);
        return entity;
    }
}
