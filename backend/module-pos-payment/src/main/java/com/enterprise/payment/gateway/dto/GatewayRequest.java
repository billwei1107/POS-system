/**
 * @file GatewayRequest.java
 * @description 閘道請求 DTO / Gateway request DTO
 * @description_en Unified request payload sent to any payment gateway implementation
 * @description_zh 傳送給所有閘道實作的統一請求資料物件
 */
package com.enterprise.payment.gateway.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GatewayRequest(
    UUID orderId,
    String orderNo,
    BigDecimal amount,
    BigDecimal tendered,
    String currency,
    String referenceNo,
    String note
) {}
