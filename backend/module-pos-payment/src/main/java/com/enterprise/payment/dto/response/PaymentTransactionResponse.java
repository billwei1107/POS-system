/**
 * @file PaymentTransactionResponse.java
 * @description 支付交易回應 DTO / Payment transaction response DTO
 * @description_en Response DTO for payment transaction results returned to frontend
 * @description_zh 回傳給前端的支付交易結果 DTO
 */
package com.enterprise.payment.dto.response;

import com.enterprise.payment.entity.PaymentTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentTransactionResponse(
    UUID id,
    UUID orderId,
    UUID storeId,
    UUID payMethodId,
    String methodType,
    BigDecimal amount,
    BigDecimal tendered,
    BigDecimal changeGiven,
    PaymentTransaction.TxnStatus status,
    String gatewayRef,
    LocalDateTime processedAt
) {
    public static PaymentTransactionResponse from(PaymentTransaction t) {
        return new PaymentTransactionResponse(
            t.getId(), t.getOrderId(), t.getStoreId(), t.getPayMethodId(),
            t.getMethodType(), t.getAmount(), t.getTendered(), t.getChangeGiven(),
            t.getStatus(), t.getGatewayRef(), t.getProcessedAt()
        );
    }
}
