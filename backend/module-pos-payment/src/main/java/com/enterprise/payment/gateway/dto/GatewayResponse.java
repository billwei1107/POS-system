/**
 * @file GatewayResponse.java
 * @description 閘道回應 DTO / Gateway response DTO
 * @description_en Unified response returned from any payment gateway after charge or refund
 * @description_zh 任何閘道在扣款或退款後回傳的統一回應資料物件
 */
package com.enterprise.payment.gateway.dto;

public record GatewayResponse(
    boolean success,
    String gatewayRef,
    String rawResponse,
    String errorCode,
    String errorMessage
) {
    public static GatewayResponse ok(String gatewayRef, String rawResponse) {
        return new GatewayResponse(true, gatewayRef, rawResponse, null, null);
    }

    public static GatewayResponse fail(String errorCode, String errorMessage) {
        return new GatewayResponse(false, null, null, errorCode, errorMessage);
    }
}
