/**
 * @file OpenShiftRequest.java
 * @description 開班請求 DTO / Open shift request DTO
 * @description_en Request payload for opening a new shift session
 * @description_zh 開班請求資料傳輸物件
 */
package com.enterprise.staff.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OpenShiftRequest(
        @NotNull UUID employeeId,
        UUID terminalId,
        BigDecimal openingCash
) {}
