package com.example.apikeyservice.service.impl;

import com.example.apikeyservice.constants.ApiKeyConstants;
import com.example.apikeyservice.dto.ApiKeyResponseDto;
import com.example.apikeyservice.entity.ApiKey;
import com.example.apikeyservice.exception.ApiKeyExpiredException;
import com.example.apikeyservice.exception.ApiKeyNotFoundException;
import com.example.apikeyservice.exception.ApiKeyValidationException;
import com.example.apikeyservice.exception.CooldownActiveException;
import com.example.apikeyservice.exception.InvalidApiKeyException;
import com.example.apikeyservice.exception.InvalidClientException;
import com.example.apikeyservice.mapper.ApiKeyMapper;
import com.example.apikeyservice.repository.ApiKeyRepository;
import com.example.apikeyservice.service.ApiKeyService;
import com.example.apikeyservice.utils.EncryptionUtils;
import com.example.apikeyservice.utils.HashUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository repository;
    private final ApiKeyMapper mapper;

    @Value("${apikey.valid-client-id}")
    private String validClientId;

    @Value("${apikey.renewed-apikey-id}")
    private String validRenewedApikeyId;

    @Value("${encryption.secret-key}")
    private String secretKey;

    @Value("${apikey.renew-cooldown-hours:24}")
    private int renewCooldownHours;

    private String validClientIdHash;
    private String validRenewedApikeyIdHash;

    public ApiKeyServiceImpl(ApiKeyRepository repository, ApiKeyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @PostConstruct
    void initHashes() {
        this.validClientIdHash = HashUtils.sha256Hex(validClientId);
        this.validRenewedApikeyIdHash = HashUtils.sha256Hex(validRenewedApikeyId);
    }

    private void validateClientId(String clientId) {
        if (clientId == null || clientId.isBlank() || !isValidClientId(clientId)) {
            throw new InvalidClientException();
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ApiKeyResponseDto createOrGetApiKey(String clientId) {
        validateClientId(clientId);

        String clientHash = HashUtils.sha256Hex(clientId);

        return findLatestForWrite(clientHash)
                .map(existing -> handleExistingApiKey(existing, clientHash))
                .orElseGet(() -> createNewApiKey(
                        clientHash,
                        ApiKeyConstants.MSG_CREATED,
                        ApiKeyConstants.CODE_CREATED));
    }

    private boolean isValidClientId(String clientId) {
        return HashUtils.sha256Hex(clientId).equals(validClientIdHash);
    }

    private ApiKeyResponseDto handleExistingApiKey(ApiKey existing, String clientHash) {
        if (existing.getExpirationAt().isBefore(LocalDateTime.now())) {
            deactivate(existing);
            return createNewApiKey(
                    clientHash,
                    ApiKeyConstants.MSG_EXPIRED_RENEWED,
                    ApiKeyConstants.CODE_EXPIRED_RENEWED);
        }
        try {
            String decrypted = EncryptionUtils.decrypt(existing.getApiKey(), secretKey);
            return ApiKeyResponseDto.success(
                    decrypted,
                    ApiKeyConstants.MSG_ALREADY_ACTIVE,
                    ApiKeyConstants.CODE_ALREADY_ACTIVE);
        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar el ApiKey", e);
        }
    }

    private ApiKeyResponseDto createNewApiKey(String clientHash, String message, String code) {
        try {
            String rawKey = mapper.generateRawApiKey();
            String encryptedKey = EncryptionUtils.encrypt(rawKey, secretKey);
            ApiKey entity = mapper.toEntity(clientHash, rawKey, encryptedKey);
            repository.save(entity);
            return ApiKeyResponseDto.success(rawKey, message, code);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el ApiKey", e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ApiKeyResponseDto forceRenewApiKey(String renewedApiKeyId, String clientId) {
        if (renewedApiKeyId == null || renewedApiKeyId.isBlank()
                || clientId == null || clientId.isBlank()) {
            throw new ApiKeyValidationException(
                    ApiKeyConstants.MSG_MISSING_PARAMS,
                    ApiKeyConstants.CODE_MISSING_PARAMS);
        }
        if (!isValidRenewedKey(renewedApiKeyId)) {
            throw new ApiKeyValidationException(
                    ApiKeyConstants.MSG_INVALID_RENEWED_ID,
                    ApiKeyConstants.CODE_INVALID_RENEWED_ID);
        }
        validateClientId(clientId);

        String clientHash = HashUtils.sha256Hex(clientId);

        findLatestForWrite(clientHash)
                .ifPresent(existing -> {
                    if (existing.getRenewCooldownUntil() != null
                            && existing.getRenewCooldownUntil().isAfter(LocalDateTime.now())) {
                        throw new CooldownActiveException();
                    }
                    deactivate(existing);
                });

        ApiKeyResponseDto response = createNewApiKey(
                clientHash,
                ApiKeyConstants.MSG_MANUAL_RENEWED,
                ApiKeyConstants.CODE_MANUAL_RENEWED);

        updateRenewCooldownForLatest(clientHash);

        return response;
    }

    private boolean isValidRenewedKey(String renewedApikeyId) {
        return HashUtils.sha256Hex(renewedApikeyId).equals(validRenewedApikeyIdHash);
    }

    private void deactivate(ApiKey entity) {
        entity.setActive("0");
        repository.saveAndFlush(entity);
    }

    private void updateRenewCooldownForLatest(String clientHash) {
        repository.findTopByClientIdOrderByCreatedAtDesc(clientHash)
                .ifPresent(apiKey -> {
                    apiKey.setRenewCooldownUntil(LocalDateTime.now().plusHours(renewCooldownHours));
                    repository.save(apiKey);
                });
    }

    private Optional<ApiKey> findLatestForWrite(String clientHash) {
        return repository.findActiveByClientIdWithLock(clientHash);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiKeyResponseDto validateApiKey(String clientId, String apiKey) {
        if (clientId == null || clientId.isBlank() || apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyValidationException(
                    ApiKeyConstants.MSG_MISSING_PARAMS,
                    ApiKeyConstants.CODE_MISSING_PARAMS);
        }
        validateClientId(clientId);

        String clientHash = HashUtils.sha256Hex(clientId);

        ApiKey activeKey = findActiveApiKey(clientHash)
                .orElseThrow(ApiKeyNotFoundException::new);

        if (isExpired(activeKey)) {
            throw new ApiKeyExpiredException();
        }

        return validateApiKeyMatch(activeKey, apiKey);
    }

    private Optional<ApiKey> findActiveApiKey(String clientHash) {
        return repository.findTopByClientIdOrderByCreatedAtDesc(clientHash)
                .filter(entity -> "1".equals(entity.getActive()));
    }

    private boolean isExpired(ApiKey entity) {
        return entity.getExpirationAt() != null
                && entity.getExpirationAt().isBefore(LocalDateTime.now());
    }

    private ApiKeyResponseDto validateApiKeyMatch(ApiKey activeKey, String providedApiKey) {
        String providedHash = HashUtils.sha256Hex(providedApiKey);
        if (!providedHash.equals(activeKey.getApiKeyHash())) {
            throw new InvalidApiKeyException();
        }
        return ApiKeyResponseDto.success(
                "true",
                ApiKeyConstants.MSG_VALID,
                ApiKeyConstants.CODE_VALID);
    }
}
