/**
 * @file GiftCard.java
 * @description 禮品卡實體 / Gift card entity
 * @description_en Represents a stored-value gift card with balance tracking
 * @description_zh 禮品卡，支援儲值、消費與餘額追蹤
 */
package com.enterprise.core.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_gift_cards")
@SQLDelete(sql = "UPDATE pos_gift_cards SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class GiftCard extends BaseEntity {

    @Column(name = "card_no", nullable = false, unique = true, length = 30)
    private String cardNo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "initial_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal initialValue;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private GiftCardStatus status = GiftCardStatus.ACTIVE;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "issued_by")
    private UUID issuedBy;

    public enum GiftCardStatus {
        ACTIVE, EXHAUSTED, EXPIRED, DISABLED
    }
}
