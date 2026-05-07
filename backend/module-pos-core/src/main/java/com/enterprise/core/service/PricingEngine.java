/**
 * @file PricingEngine.java
 * @description 定價計算引擎 / Pricing calculation engine
 * @description_en Calculates subtotal, discounts, tax and rounding for an order using BigDecimal
 * @description_zh 使用 BigDecimal 計算訂單小計、折扣、稅額與尾數調整，禁止使用浮點數
 */
package com.enterprise.core.service;

import com.enterprise.core.dto.request.OrderItemRequest;
import com.enterprise.core.dto.response.PricingResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PricingEngine {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");
    private static final int SCALE = 2;

    // ========================================
    // 計算訂單總價 / Calculate order totals
    // ========================================
    public PricingResult calculate(List<OrderItemRequest> items, BigDecimal discountAmount, boolean taxIncluded) {
        BigDecimal subtotal = computeSubtotal(items);
        BigDecimal discount = discountAmount != null ? discountAmount.setScale(SCALE, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal afterDiscount = subtotal.subtract(discount).max(BigDecimal.ZERO);

        BigDecimal taxTotal;
        BigDecimal netBeforeTax;
        if (taxIncluded) {
            // 含稅價拆算稅額 / Extract tax from tax-inclusive price
            taxTotal = afterDiscount.multiply(TAX_RATE)
                    .divide(BigDecimal.ONE.add(TAX_RATE), SCALE, RoundingMode.HALF_UP);
            netBeforeTax = afterDiscount.subtract(taxTotal);
        } else {
            // 外加稅 / Add tax on top
            netBeforeTax = afterDiscount;
            taxTotal = afterDiscount.multiply(TAX_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal grandTotal = netBeforeTax.add(taxTotal);
        // 四捨五入尾數調整（NT$ 無小數）/ Rounding adjustment for TWD
        BigDecimal rounded = grandTotal.setScale(0, RoundingMode.HALF_UP).setScale(SCALE);
        BigDecimal roundingAdj = rounded.subtract(grandTotal);

        return new PricingResult(subtotal, discount, taxTotal, roundingAdj, rounded);
    }

    // ========================================
    // 計算小計 / Compute subtotal from items
    // ========================================
    public BigDecimal computeSubtotal(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .map(item -> {
                    BigDecimal modifierAdj = item.modifierPriceAdjustment() != null
                            ? item.modifierPriceAdjustment() : BigDecimal.ZERO;
                    BigDecimal unitPrice = item.unitPrice().add(modifierAdj);
                    return unitPrice.multiply(item.quantity()).setScale(SCALE, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========================================
    // 計算找零 / Calculate change given
    // ========================================
    public BigDecimal calculateChange(BigDecimal tendered, BigDecimal grandTotal) {
        if (tendered == null) return BigDecimal.ZERO;
        return tendered.subtract(grandTotal).max(BigDecimal.ZERO).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
