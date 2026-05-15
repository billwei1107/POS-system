/**
 * @file PointAdjustmentRequest.java
 * @description 會員點數手動調整請求 DTO / Member point manual adjustment request DTO
 * @description_en Carries a positive or negative point delta for manager adjustments
 * @description_zh 承載店長手動調整會員點數的正負異動值
 */
package com.enterprise.crm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PointAdjustmentRequest(
        @NotNull(message = "點數異動不得為空")
        Integer pointsDelta,

        @Size(max = 255, message = "備註不可超過 255 字")
        String note
) {
}
