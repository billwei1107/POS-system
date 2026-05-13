/**
 * @file GenerateZReportRequest.java
 * @description Z Report 產生請求 DTO / Generate Z Report request DTO
 * @description_en Request payload for generating the daily Z Report
 * @description_zh 產生日結 Z Report 的請求資料
 */
package com.enterprise.staff.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GenerateZReportRequest(
        @NotNull LocalDate reportDate,
        @NotNull BigDecimal cashInDrawer,
        @NotNull UUID generatedBy
) {}
