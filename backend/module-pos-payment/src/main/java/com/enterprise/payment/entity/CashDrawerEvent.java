/**
 * @file CashDrawerEvent.java
 * @description 現金抽屜事件 Entity / Cash drawer audit event entity
 * @description_en Immutable audit log for every cash drawer operation (open/close/sale/etc.)
 * @description_zh 現金抽屜每次操作（開啟/關閉/銷售/退款/收付款）的不可變稽核日誌
 */
package com.enterprise.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_cash_drawer_events")
@Getter
@Setter
public class CashDrawerEvent {

    public enum EventType { OPEN, CLOSE, SALE, REFUND, PAY_IN, PAY_OUT, NO_SALE }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID drawerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventType eventType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    private UUID orderId;

    @Column(nullable = false)
    private UUID employeeId;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
