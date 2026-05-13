/**
 * @file PricingResult.java
 * @description 定價計算結果 DTO / Pricing calculation result DTO
 * @description_en Immutable value object returned by PricingEngine
 * @description_zh PricingEngine 回傳的不可變計算結果
 */
package com.enterprise.core.dto.response;

import java.math.BigDecimal;

public record PricingResult(
    BigDecimal subtotal,
    BigDecimal discountTotal,
    BigDecimal taxTotal,
    BigDecimal roundingAdj,
    BigDecimal grandTotal
) {}
