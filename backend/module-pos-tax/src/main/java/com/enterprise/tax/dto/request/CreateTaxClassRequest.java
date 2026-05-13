/**
 * @file CreateTaxClassRequest.java
 * @description 建立稅率類別請求 DTO / Create tax class request DTO
 * @description_en Payload for creating a new tax class for a store
 * @description_zh 為門店建立新稅率類別的請求資料
 */
package com.enterprise.tax.dto.request;

import com.enterprise.tax.entity.TaxClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTaxClassRequest(
        @NotNull UUID storeId,
        @NotBlank String name,
        @NotNull TaxClass.TaxType taxType,
        @NotNull BigDecimal rate,
        String description,
        boolean isDefault
) {}
