/**
 * @file MemberRequest.java
 * @description 會員建立請求 DTO / Member create request DTO
 * @description_en Request payload for quick POS member registration
 * @description_zh POS 快速註冊會員的請求資料
 */
package com.enterprise.crm.dto;

import com.enterprise.crm.entity.Member;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MemberRequest(
        String memberNo,

        @NotBlank(message = "會員姓名不得為空")
        String name,

        @NotBlank(message = "會員電話不得為空")
        String phone,

        String email,
        LocalDate birthday,
        String cardNo,
        String barcode,
        Member.Tier tier,

        @DecimalMin(value = "0.00", message = "折扣百分比不得為負數")
        BigDecimal discountPercent
) {
}
