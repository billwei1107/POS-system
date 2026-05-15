/**
 * @file CreateGatewayConfigRequest.java
 * @description 建立支付閘道設定請求 DTO / Create payment gateway config request DTO
 * @description_en Request body for creating store-level gateway configuration
 * @description_zh 建立門店層級支付閘道設定的請求資料
 */
package com.enterprise.payment.dto.request;

import com.enterprise.payment.entity.GatewayConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateGatewayConfigRequest(
    @NotNull UUID storeId,
    @NotNull GatewayConfig.GatewayType gatewayType,
    @NotBlank String displayName,
    String merchantId,
    String apiKey,
    String apiSecret,
    String endpointUrl,
    String extraConfig,
    boolean isSandbox,
    boolean isActive
) {}
