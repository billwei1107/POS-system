/**
 * @file CreateRefundRequest.java
 * @description 建立退款請求 DTO / Create refund request DTO
 * @description_en Request payload for initiating a refund on an existing order
 * @description_zh 對現有訂單發起退款的請求資料
 */
package com.enterprise.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRefundRequest(
    @NotNull UUID orderId,
    @NotNull @Positive BigDecimal refundAmount,
    @NotBlank String refundMethod,
    String reason,
    UUID approvedBy
) {}
