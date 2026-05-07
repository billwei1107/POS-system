/**
 * @file CashDrawer.java
 * @description 現金抽屜 Entity / Cash drawer entity
 * @description_en Tracks a single open/close session of a cash drawer on a terminal
 * @description_zh 記錄單次現金抽屜開啟/關閉會話，含開收金額與差異
 */
package com.enterprise.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_cash_drawers")
@Getter
@Setter
public class CashDrawer {

    public enum DrawerStatus { OPEN, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private UUID terminalId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal openingAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal closingAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal expectedAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal variance;

    @Column(nullable = false)
    private UUID openedBy;

    private UUID closedBy;

    @Column(nullable = false)
    private LocalDateTime openedAt = LocalDateTime.now();

    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DrawerStatus status = DrawerStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
