/**
 * @file ProcessPaymentRequest.java
 * @description 支付處理請求 DTO / Process payment request DTO
 * @description_en Request body for manual payment processing (non-OrderCompletedEvent path)
 * @description_zh 手動觸發支付的請求 DTO（非 OrderCompletedEvent 自動流程）
 */
package com.enterprise.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentRequest(
    @NotNull UUID orderId,
    @NotNull String orderNo,
    @NotNull UUID storeId,
    @NotNull UUID payMethodId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    BigDecimal tendered,
    String currency,
    String note
) {}
