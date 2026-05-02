package com.example.apikeyservice.service.impl;

import com.example.apikeyservice.constants.ApiKeyConstants;
import com.example.apikeyservice.dto.ApiKeyResponseDto;
import com.example.apikeyservice.entity.ApiKey;
import com.example.apikeyservice.exception.*;
import com.example.apikeyservice.mapper.ApiKeyMapper;
import com.example.apikeyservice.repository.ApiKeyRepository;
import com.example.apikeyservice.utils.EncryptionUtils;
import com.example.apikeyservice.utils.HashUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceImplTest {

    @Mock
    private ApiKeyRepository repository;

    @Mock
    private ApiKeyMapper mapper;

    @InjectMocks
    private ApiKeyServiceImpl service;

    private static final String CLIENT_ID  = "CLIENT-001";
    private static final String RENEW_ID   = "RENEW-SECRET-001";
    private static final String SECRET_KEY = "A1B2C3D4E5F60708";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "validClientId", CLIENT_ID);
        ReflectionTestUtils.setField(service, "validRenewedApikeyId", RENEW_ID);
        ReflectionTestUtils.setField(service, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(service, "renewCooldownHours", 24);
        service.initHashes();
    }

    // createOrGetApiKey

    @Test
    void createOrGetApiKey_nullClientId_throwsInvalidClientException() {
        assertThrows(InvalidClientException.class,
                () -> service.createOrGetApiKey(null));
    }

    @Test
    void createOrGetApiKey_invalidClientId_throwsInvalidClientException() {
        assertThrows(InvalidClientException.class,
                () -> service.createOrGetApiKey("wrong-client"));
    }

    @Test
    void createOrGetApiKey_noExistingKey_createsNew() {
        when(repository.findActiveByClientIdWithLock(anyString())).thenReturn(Optional.empty());
        when(mapper.generateRawApiKey()).thenReturn("rawkey123");
        when(mapper.toEntity(anyString(), anyString(), anyString())).thenReturn(new ApiKey());

        ApiKeyResponseDto result = service.createOrGetApiKey(CLIENT_ID);

        assertTrue(result.success());
        assertEquals(ApiKeyConstants.CODE_CREATED, result.code());
        verify(repository).save(any(ApiKey.class));
    }

    @Test
    void createOrGetApiKey_existingActiveKey_returnsExistingDecrypted() throws Exception {
        String rawKey = "existing-raw-key";
        ApiKey existing = new ApiKey();
        existing.setApiKey(EncryptionUtils.encrypt(rawKey, SECRET_KEY));
        existing.setExpirationAt(LocalDateTime.now().plusDays(7));

        when(repository.findActiveByClientIdWithLock(anyString())).thenReturn(Optional.of(existing));

        ApiKeyResponseDto result = service.createOrGetApiKey(CLIENT_ID);

        assertTrue(result.success());
        assertEquals(ApiKeyConstants.CODE_ALREADY_ACTIVE, result.code());
        assertEquals(rawKey, result.apiKey());
    }

    @Test
    void createOrGetApiKey_existingExpiredKey_deactivatesAndCreatesNew() throws Exception {
        ApiKey expired = new ApiKey();
        expired.setApiKey(EncryptionUtils.encrypt("old-key", SECRET_KEY));
        expired.setExpirationAt(LocalDateTime.now().minusDays(1));

        when(repository.findActiveByClientIdWithLock(anyString())).thenReturn(Optional.of(expired));
        when(mapper.generateRawApiKey()).thenReturn("new-raw-key");
        when(mapper.toEntity(anyString(), anyString(), anyString())).thenReturn(new ApiKey());

        ApiKeyResponseDto result = service.createOrGetApiKey(CLIENT_ID);

        assertTrue(result.success());
        assertEquals(ApiKeyConstants.CODE_EXPIRED_RENEWED, result.code());
        verify(repository).saveAndFlush(expired);
    }

    // forceRenewApiKey

    @Test
    void forceRenewApiKey_nullParams_throwsMissingParamsException() {
        ApiKeyValidationException ex = assertThrows(ApiKeyValidationException.class,
                () -> service.forceRenewApiKey(null, CLIENT_ID));
        assertEquals(ApiKeyConstants.CODE_MISSING_PARAMS, ex.getCode());
    }

    @Test
    void forceRenewApiKey_invalidRenewId_throwsInvalidRenewedIdException() {
        ApiKeyValidationException ex = assertThrows(ApiKeyValidationException.class,
                () -> service.forceRenewApiKey("wrong-renew-id", CLIENT_ID));
        assertEquals(ApiKeyConstants.CODE_INVALID_RENEWED_ID, ex.getCode());
    }

    @Test
    void forceRenewApiKey_cooldownActive_throwsCooldownException() {
        ApiKey existing = new ApiKey();
        existing.setRenewCooldownUntil(LocalDateTime.now().plusHours(12));

        when(repository.findActiveByClientIdWithLock(anyString())).thenReturn(Optional.of(existing));

        assertThrows(CooldownActiveException.class,
                () -> service.forceRenewApiKey(RENEW_ID, CLIENT_ID));
    }

    @Test
    void forceRenewApiKey_validParams_createsNewKey() {
        ApiKey existing = new ApiKey();
        existing.setRenewCooldownUntil(LocalDateTime.now().minusHours(1));

        when(repository.findActiveByClientIdWithLock(anyString())).thenReturn(Optional.of(existing));
        when(mapper.generateRawApiKey()).thenReturn("new-raw-key");
        when(mapper.toEntity(anyString(), anyString(), anyString())).thenReturn(new ApiKey());
        when(repository.findTopByClientIdOrderByCreatedAtDesc(anyString())).thenReturn(Optional.of(new ApiKey()));

        ApiKeyResponseDto result = service.forceRenewApiKey(RENEW_ID, CLIENT_ID);

        assertTrue(result.success());
        assertEquals(ApiKeyConstants.CODE_MANUAL_RENEWED, result.code());
    }

    // validateApiKey ─────────────────────────────────────────────────────

    @Test
    void validateApiKey_missingParams_throwsMissingParamsException() {
        ApiKeyValidationException ex = assertThrows(ApiKeyValidationException.class,
                () -> service.validateApiKey(CLIENT_ID, null));
        assertEquals(ApiKeyConstants.CODE_MISSING_PARAMS, ex.getCode());
    }

    @Test
    void validateApiKey_noActiveKey_throwsNotFoundException() {
        when(repository.findTopByClientIdOrderByCreatedAtDesc(anyString())).thenReturn(Optional.empty());

        assertThrows(ApiKeyNotFoundException.class,
                () -> service.validateApiKey(CLIENT_ID, "some-key"));
    }

    @Test
    void validateApiKey_expiredKey_throwsExpiredException() {
        ApiKey expired = new ApiKey();
        expired.setActive("1");
        expired.setExpirationAt(LocalDateTime.now().minusDays(1));
        expired.setApiKeyHash(HashUtils.sha256Hex("some-key"));

        when(repository.findTopByClientIdOrderByCreatedAtDesc(anyString())).thenReturn(Optional.of(expired));

        assertThrows(ApiKeyExpiredException.class,
                () -> service.validateApiKey(CLIENT_ID, "some-key"));
    }

    @Test
    void validateApiKey_wrongKey_throwsInvalidApiKeyException() {
        ApiKey active = new ApiKey();
        active.setActive("1");
        active.setExpirationAt(LocalDateTime.now().plusDays(7));
        active.setApiKeyHash(HashUtils.sha256Hex("correct-key"));

        when(repository.findTopByClientIdOrderByCreatedAtDesc(anyString())).thenReturn(Optional.of(active));

        assertThrows(InvalidApiKeyException.class,
                () -> service.validateApiKey(CLIENT_ID, "wrong-key"));
    }

    @Test
    void validateApiKey_validKey_returnsValid() {
        String rawKey = "valid-test-key";
        ApiKey active = new ApiKey();
        active.setActive("1");
        active.setExpirationAt(LocalDateTime.now().plusDays(7));
        active.setApiKeyHash(HashUtils.sha256Hex(rawKey));

        when(repository.findTopByClientIdOrderByCreatedAtDesc(anyString())).thenReturn(Optional.of(active));

        ApiKeyResponseDto result = service.validateApiKey(CLIENT_ID, rawKey);

        assertTrue(result.success());
        assertEquals(ApiKeyConstants.CODE_VALID, result.code());
    }
}
