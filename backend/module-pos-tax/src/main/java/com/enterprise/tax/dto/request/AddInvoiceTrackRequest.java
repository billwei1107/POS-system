/**
 * @file AddInvoiceTrackRequest.java
 * @description 新增發票字軌請求 DTO / Add invoice track request DTO
 * @description_en Input when registering a new bi-monthly character track from MoF allocation
 * @description_zh 登記財政部配發的新一期發票字軌號碼範圍
 */
package com.enterprise.tax.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddInvoiceTrackRequest(
        @NotNull UUID storeId,
        @NotBlank String sellerId,
        @NotBlank String trackPrefix,
        @NotBlank String yearMonth,
        @NotBlank String period,
        @NotBlank String startNo,
        @NotBlank String endNo
) {}
