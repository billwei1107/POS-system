/**
 * @file OrderStateMachineTest.java
 * @description OrderStateMachine 單元測試 / Order state machine unit tests
 * @description_en Tests legal and illegal state transitions in the order lifecycle
 * @description_zh 測試訂單生命週期中合法與非法狀態轉換
 */
package com.enterprise.core.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.core.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateMachineTest {

    private OrderStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new OrderStateMachine();
    }

    // ========================================
    // 合法轉換 / Legal transitions
    // ========================================
    @Test
    void transition_draftToConfirmed_succeeds() {
        Order order = createOrder(Order.OrderStatus.DRAFT);
        stateMachine.transition(order, Order.OrderStatus.CONFIRMED);
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
    }

    @Test
    void transition_confirmedToReady_succeeds() {
        Order order = createOrder(Order.OrderStatus.CONFIRMED);
        stateMachine.transition(order, Order.OrderStatus.PREPARING);
        stateMachine.transition(order, Order.OrderStatus.READY);
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.READY);
    }

    @Test
    void transition_readyToCompleted_succeeds() {
        Order order = createOrder(Order.OrderStatus.READY);
        stateMachine.transition(order, Order.OrderStatus.COMPLETED);
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
    }

    @Test
    void transition_completedToClosed_succeeds() {
        Order order = createOrder(Order.OrderStatus.COMPLETED);
        stateMachine.transition(order, Order.OrderStatus.CLOSED);
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CLOSED);
    }

    @Test
    void transition_draftToVoided_succeeds() {
        Order order = createOrder(Order.OrderStatus.DRAFT);
        stateMachine.transition(order, Order.OrderStatus.VOIDED);
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.VOIDED);
    }

    // ========================================
    // 非法轉換 / Illegal transitions
    // ========================================
    @Test
    void transition_draftToCompleted_throwsException() {
        Order order = createOrder(Order.OrderStatus.DRAFT);
        assertThatThrownBy(() -> stateMachine.transition(order, Order.OrderStatus.COMPLETED))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("illegal transition");
    }

    @Test
    void transition_closedToAny_throwsException() {
        Order order = createOrder(Order.OrderStatus.CLOSED);
        assertThatThrownBy(() -> stateMachine.transition(order, Order.OrderStatus.VOIDED))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void transition_voidedToAny_throwsException() {
        Order order = createOrder(Order.OrderStatus.VOIDED);
        assertThatThrownBy(() -> stateMachine.transition(order, Order.OrderStatus.COMPLETED))
            .isInstanceOf(BusinessException.class);
    }

    // ========================================
    // canTransition 工具方法 / canTransition utility
    // ========================================
    @Test
    void canTransition_draftToConfirmed_returnsTrue() {
        assertThat(stateMachine.canTransition(Order.OrderStatus.DRAFT, Order.OrderStatus.CONFIRMED)).isTrue();
    }

    @Test
    void canTransition_completedToDraft_returnsFalse() {
        assertThat(stateMachine.canTransition(Order.OrderStatus.COMPLETED, Order.OrderStatus.DRAFT)).isFalse();
    }

    private Order createOrder(Order.OrderStatus status) {
        Order order = new Order();
        order.setOrderNo("TEST-001");
        order.setStatus(status);
        return order;
    }
}
