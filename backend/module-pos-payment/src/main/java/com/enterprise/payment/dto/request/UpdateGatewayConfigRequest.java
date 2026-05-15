/**
 * @file UpdateGatewayConfigRequest.java
 * @description 更新支付閘道設定請求 DTO / Update payment gateway config request DTO
 * @description_en Request body for updating store-level gateway configuration without exposing stored secrets
 * @description_zh 更新門店支付閘道設定，避免前端必須回傳既有密鑰
 */
package com.enterprise.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateGatewayConfigRequest(
    @NotBlank String displayName,
    String merchantId,
    String apiKey,
    String apiSecret,
    String endpointUrl,
    String extraConfig,
    boolean isSandbox,
    boolean isActive
) {}
