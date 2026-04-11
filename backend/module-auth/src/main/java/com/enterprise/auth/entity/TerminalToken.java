package com.enterprise.auth.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 終端機認證 Token 實體 / Terminal device registration token entity
 */
@Entity
@Table(name = "auth_terminal_tokens")
@Data
@EqualsAndHashCode(callSuper = true)
public class TerminalToken extends BaseEntity {

    @Column(name = "terminal_id", nullable = false)
    private UUID terminalId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "registered_by")
    private UUID registeredBy;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(nullable = false)
    private Boolean active = true;
}
