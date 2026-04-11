package com.enterprise.auth.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * POS PIN 碼實體 / POS PIN code entity for terminal quick login
 */
@Entity
@Table(name = "auth_pin_codes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "terminal_type"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class PinCode extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(name = "terminal_type", nullable = false, length = 20)
    private String terminalType = "POS";

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
