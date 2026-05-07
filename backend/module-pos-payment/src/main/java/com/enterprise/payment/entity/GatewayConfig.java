/**
 * @file GatewayConfig.java
 * @description 支付閘道配置 Entity / Payment gateway configuration entity
 * @description_en Store-level gateway credentials and settings (api_key stored encrypted in production)
 * @description_zh 門店層級的閘道憑證與設定，api_key 於正式環境加密存儲
 */
package com.enterprise.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_gateway_configs")
@SQLDelete(sql = "UPDATE pos_gateway_configs SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class GatewayConfig {

    public enum GatewayType { CASH, MOCK_CARD, LINE_PAY, JKOPAY, TAIWAN_PAY }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GatewayType gatewayType;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(length = 200)
    private String merchantId;

    @Column(length = 500)
    private String apiKey;

    @Column(length = 500)
    private String apiSecret;

    @Column(length = 500)
    private String endpointUrl;

    @Column(columnDefinition = "TEXT")
    private String extraConfig;

    @Column(nullable = false)
    private boolean isSandbox = true;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime deletedAt;

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
