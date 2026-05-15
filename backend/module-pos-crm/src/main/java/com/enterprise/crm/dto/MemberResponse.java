/**
 * @file MemberResponse.java
 * @description 會員回應 DTO / Member response DTO
 * @description_en Response payload for POS member lookup and binding
 * @description_zh POS 會員查詢與綁定使用的回應資料
 */
package com.enterprise.crm.dto;

import com.enterprise.crm.entity.Member;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        String memberNo,
        String name,
        String phone,
        String phoneMasked,
        String email,
        LocalDate birthday,
        String cardNo,
        String barcode,
        Member.Tier tier,
        String tierLabel,
        BigDecimal discountPercent,
        Integer pointsBalance,
        BigDecimal storedValueBalance,
        BigDecimal annualSpend,
        Boolean active
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getMemberNo(),
                member.getName(),
                member.getPhone(),
                maskPhone(member.getPhone()),
                member.getEmail(),
                member.getBirthday(),
                member.getCardNo(),
                member.getBarcode(),
                member.getTier(),
                tierLabel(member.getTier()),
                member.getDiscountPercent(),
                member.getPointsBalance(),
                member.getStoredValueBalance(),
                member.getAnnualSpend(),
                member.getActive()
        );
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 4) + "-***-" + phone.substring(phone.length() - 3);
    }

    private static String tierLabel(Member.Tier tier) {
        return switch (tier) {
            case PLATINUM -> "白金";
            case GOLD -> "金卡";
            case SILVER -> "銀卡";
            case BRONZE -> "一般";
        };
    }
}
