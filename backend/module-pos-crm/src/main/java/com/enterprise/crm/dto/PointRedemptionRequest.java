/**
 * @file PointRedemptionRequest.java
 * @description 會員點數兌換請求 DTO / Member point redemption request DTO
 * @description_en Carries loyalty points to redeem during POS checkout
 * @description_zh 承載 POS 結帳時會員要折抵的點數
 */
package com.enterprise.crm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PointRedemptionRequest(
        @NotNull(message = "兌換點數不得為空")
        @Positive(message = "兌換點數必須大於 0")
        Integer points,

        UUID orderId,

        @Size(max = 255, message = "備註不可超過 255 字")
        String note
) {
}
