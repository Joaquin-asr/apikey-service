package com.example.apikeyservice.repository;

import com.example.apikeyservice.entity.ApiKey;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findTopByClientIdOrderByCreatedAtDesc(String clientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT a FROM ApiKey a WHERE a.clientId = :clientId AND a.active = '1'")
    Optional<ApiKey> findActiveByClientIdWithLock(@Param("clientId") String clientId);
}
