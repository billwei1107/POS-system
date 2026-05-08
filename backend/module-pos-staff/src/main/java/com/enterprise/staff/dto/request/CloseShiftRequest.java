/**
 * @file CloseShiftRequest.java
 * @description 關班請求 DTO / Close shift request DTO
 * @description_en Request payload for closing a shift with cash count
 * @description_zh 關班請求，包含現金清點金額
 */
package com.enterprise.staff.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CloseShiftRequest(
        @NotNull BigDecimal closingCash,
        String notes
) {}
