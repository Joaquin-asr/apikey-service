package com.example.apikeyservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;

    @Column(name = "api_key", nullable = false, length = 512)
    private String apiKey;

    @Column(name = "api_key_hash", nullable = false, length = 64)
    private String apiKeyHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiration_at", nullable = false)
    private LocalDateTime expirationAt;

    @Column(name = "active", nullable = false, length = 1)
    private String active;

    @Column(name = "renew_cooldown_until")
    private LocalDateTime renewCooldownUntil;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiKeyHash() { return apiKeyHash; }
    public void setApiKeyHash(String apiKeyHash) { this.apiKeyHash = apiKeyHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getExpirationAt() { return expirationAt; }
    public void setExpirationAt(LocalDateTime expirationAt) { this.expirationAt = expirationAt; }

    public String getActive() { return active; }
    public void setActive(String active) { this.active = active; }

    public LocalDateTime getRenewCooldownUntil() { return renewCooldownUntil; }
    public void setRenewCooldownUntil(LocalDateTime renewCooldownUntil) { this.renewCooldownUntil = renewCooldownUntil; }
}
