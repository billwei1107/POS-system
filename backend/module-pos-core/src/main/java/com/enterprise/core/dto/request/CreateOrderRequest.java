/**
 * @file CreateOrderRequest.java
 * @description 建立訂單請求 DTO / Create order request DTO
 * @description_en Request payload for creating a new POS order
 * @description_zh 建立新 POS 訂單的請求資料
 */
package com.enterprise.core.dto.request;

import com.enterprise.core.entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
    @NotNull UUID storeId,
    UUID terminalId,
    UUID employeeId,
    Order.OrderType orderType,
    @NotEmpty @Valid List<OrderItemRequest> items,
    BigDecimal discountAmount,
    UUID memberId,
    String tableNo,
    Integer guestCount,
    String note,
    boolean taxIncluded
) {}
