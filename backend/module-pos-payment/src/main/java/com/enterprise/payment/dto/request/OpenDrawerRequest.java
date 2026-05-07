/**
 * @file OpenDrawerRequest.java
 * @description 開啟現金抽屜請求 DTO / Open cash drawer request DTO
 * @description_en Request body for starting a new cash drawer session at a terminal
 * @description_zh 在終端機開啟新現金抽屜會話的請求 DTO
 */
package com.enterprise.payment.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OpenDrawerRequest(
    @NotNull UUID storeId,
    @NotNull UUID terminalId,
    @NotNull UUID openedBy,
    BigDecimal openingAmount
) {}
