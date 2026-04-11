package com.enterprise.organization.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * POS 終端機實體 / POS Terminal entity
 */
@Entity
@Table(name = "org_terminals")
@Data
@EqualsAndHashCode(callSuper = true)
public class Terminal extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "terminal_code", nullable = false, unique = true, length = 50)
    private String terminalCode;

    @Column(length = 100)
    private String name;

    @Column(name = "device_type", nullable = false, length = 30)
    private String deviceType = "POS";

    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "hardware_profile_json", columnDefinition = "jsonb")
    private String hardwareProfileJson;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(nullable = false, length = 20)
    private String status = "OFFLINE";

    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt = LocalDateTime.now();
}
