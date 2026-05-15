/**
 * @file Member.java
 * @description POS 會員實體 / POS member entity
 * @description_en Stores POS customer profile, tier, discount and loyalty balances
 * @description_zh 儲存 POS 會員資料、等級、折扣與點數 / 儲值餘額
 */
package com.enterprise.crm.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pos_crm_members")
@SQLDelete(sql = "UPDATE pos_crm_members SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Member extends BaseEntity {

    @Column(name = "member_no", nullable = false, unique = true, length = 50)
    private String memberNo;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 30, unique = true)
    private String phone;

    @Column(length = 160)
    private String email;

    private LocalDate birthday;

    @Column(name = "card_no", length = 80, unique = true)
    private String cardNo;

    @Column(length = 120, unique = true)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tier tier = Tier.BRONZE;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "points_balance", nullable = false)
    private Integer pointsBalance = 0;

    @Column(name = "stored_value_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal storedValueBalance = BigDecimal.ZERO;

    @Column(name = "annual_spend", nullable = false, precision = 12, scale = 2)
    private BigDecimal annualSpend = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean active = true;

    public enum Tier {
        BRONZE, SILVER, GOLD, PLATINUM
    }
}
