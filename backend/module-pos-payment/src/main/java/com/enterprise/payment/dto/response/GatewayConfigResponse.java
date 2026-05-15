/**
 * @file GatewayConfigResponse.java
 * @description 支付閘道設定回應 DTO / Payment gateway config response DTO
 * @description_en Response DTO that hides gateway secrets and only exposes credential presence flags
 * @description_zh 回傳支付閘道設定時隱藏密鑰，只提供是否已設定的旗標
 */
package com.enterprise.payment.dto.response;

import com.enterprise.payment.entity.GatewayConfig;

import java.time.LocalDateTime;
import java.util.UUID;

public record GatewayConfigResponse(
    UUID id,
    UUID storeId,
    GatewayConfig.GatewayType gatewayType,
    String displayName,
    String merchantId,
    String endpointUrl,
    String extraConfig,
    boolean apiKeyConfigured,
    boolean apiSecretConfigured,
    boolean isSandbox,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static GatewayConfigResponse from(GatewayConfig config) {
        return new GatewayConfigResponse(
            config.getId(),
            config.getStoreId(),
            config.getGatewayType(),
            config.getDisplayName(),
            config.getMerchantId(),
            config.getEndpointUrl(),
            config.getExtraConfig(),
            hasText(config.getApiKey()),
            hasText(config.getApiSecret()),
            config.isSandbox(),
            config.isActive(),
            config.getCreatedAt(),
            config.getUpdatedAt()
        );
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
