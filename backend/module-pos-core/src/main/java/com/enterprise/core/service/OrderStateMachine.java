/**
 * @file OrderStateMachine.java
 * @description 訂單狀態機 / Order lifecycle state machine
 * @description_en Validates and enforces legal order status transitions
 * @description_zh 驗證並強制執行訂單狀態轉換規則，防止非法狀態跳轉
 */
package com.enterprise.core.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.core.entity.Order;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class OrderStateMachine {

    // 合法的狀態轉換表 / Legal transition table: from → allowed targets
    private static final Map<Order.OrderStatus, Set<Order.OrderStatus>> TRANSITIONS =
            new EnumMap<>(Order.OrderStatus.class);

    static {
        TRANSITIONS.put(Order.OrderStatus.DRAFT,
                EnumSet.of(Order.OrderStatus.CONFIRMED, Order.OrderStatus.VOIDED));
        TRANSITIONS.put(Order.OrderStatus.CONFIRMED,
                EnumSet.of(Order.OrderStatus.PREPARING, Order.OrderStatus.VOIDED));
        TRANSITIONS.put(Order.OrderStatus.PREPARING,
                EnumSet.of(Order.OrderStatus.READY, Order.OrderStatus.VOIDED));
        TRANSITIONS.put(Order.OrderStatus.READY,
                EnumSet.of(Order.OrderStatus.COMPLETED, Order.OrderStatus.VOIDED));
        TRANSITIONS.put(Order.OrderStatus.COMPLETED,
                EnumSet.of(Order.OrderStatus.CLOSED));
        TRANSITIONS.put(Order.OrderStatus.CLOSED, EnumSet.noneOf(Order.OrderStatus.class));
        TRANSITIONS.put(Order.OrderStatus.VOIDED, EnumSet.noneOf(Order.OrderStatus.class));
    }

    // ========================================
    // 驗證並執行狀態轉換 / Validate and apply transition
    // ========================================
    public void transition(Order order, Order.OrderStatus target) {
        Order.OrderStatus current = order.getStatus();
        Set<Order.OrderStatus> allowed = TRANSITIONS.getOrDefault(current, EnumSet.noneOf(Order.OrderStatus.class));
        if (!allowed.contains(target)) {
            throw new BusinessException(
                String.format("Order %s: illegal transition %s → %s", order.getOrderNo(), current, target)
            );
        }
        order.setStatus(target);
    }

    public boolean canTransition(Order.OrderStatus from, Order.OrderStatus to) {
        return TRANSITIONS.getOrDefault(from, EnumSet.noneOf(Order.OrderStatus.class)).contains(to);
    }
}
