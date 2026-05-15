/**
 * @file OrderResponse.java
 * @description 訂單回應 DTO / Order response DTO
 * @description_en Response payload for a single POS order with items and payments
 * @description_zh 單筆 POS 訂單回應，含明細與付款記錄
 */
package com.enterprise.core.dto.response;

import com.enterprise.core.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String orderNo,
    UUID storeId,
    UUID terminalId,
    UUID employeeId,
    Order.OrderStatus status,
    Order.OrderType orderType,
    BigDecimal subtotal,
    BigDecimal discountTotal,
    Order.DiscountSource discountSource,
    UUID promotionRuleId,
    String promotionCode,
    String discountLabel,
    BigDecimal taxTotal,
    BigDecimal roundingAdj,
    BigDecimal grandTotal,
    BigDecimal paidTotal,
    BigDecimal changeGiven,
    UUID memberId,
    String note,
    String tableNo,
    Integer guestCount,
    LocalDateTime completedAt,
    LocalDateTime createdAt,
    List<OrderItemResponse> items
) {
    public static OrderResponse from(Order o, List<OrderItemResponse> items) {
        return new OrderResponse(
            o.getId(), o.getOrderNo(), o.getStoreId(), o.getTerminalId(), o.getEmployeeId(),
            o.getStatus(), o.getOrderType(),
            o.getSubtotal(), o.getDiscountTotal(), o.getDiscountSource(), o.getPromotionRuleId(),
            o.getPromotionCode(), o.getDiscountLabel(), o.getTaxTotal(), o.getRoundingAdj(),
            o.getGrandTotal(), o.getPaidTotal(), o.getChangeGiven(),
            o.getMemberId(), o.getNote(), o.getTableNo(), o.getGuestCount(),
            o.getCompletedAt(), o.getCreatedAt(), items
        );
    }
}
