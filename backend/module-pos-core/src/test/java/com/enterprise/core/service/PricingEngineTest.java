/**
 * @file PricingEngineTest.java
 * @description PricingEngine 單元測試 / PricingEngine unit tests
 * @description_en Unit tests for pricing calculation covering tax, discount, rounding and change
 * @description_zh 覆蓋含稅、折扣、尾數調整與找零計算的 PricingEngine 單元測試
 */
package com.enterprise.core.service;

import com.enterprise.core.dto.request.OrderItemRequest;
import com.enterprise.core.dto.response.PricingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PricingEngineTest {

    private PricingEngine pricingEngine;

    @BeforeEach
    void setUp() {
        pricingEngine = new PricingEngine();
    }

    // ========================================
    // 基本小計計算 / Basic subtotal calculation
    // ========================================
    @Test
    void computeSubtotal_singleItem_returnsCorrect() {
        var item = new OrderItemRequest(
            null, null, "Cola", "SKU001",
            new BigDecimal("30.00"), new BigDecimal("2"),
            BigDecimal.ZERO, null, null
        );
        BigDecimal subtotal = pricingEngine.computeSubtotal(List.of(item));
        assertThat(subtotal).isEqualByComparingTo("60.00");
    }

    @Test
    void computeSubtotal_withModifierAdjustment_addedToUnitPrice() {
        var item = new OrderItemRequest(
            null, null, "Coffee", "SKU002",
            new BigDecimal("50.00"), new BigDecimal("1"),
            new BigDecimal("10.00"), null, null
        );
        BigDecimal subtotal = pricingEngine.computeSubtotal(List.of(item));
        assertThat(subtotal).isEqualByComparingTo("60.00");
    }

    // ========================================
    // 外加稅計算 / Tax exclusive calculation
    // ========================================
    @Test
    void calculate_taxExclusive_grandTotalIncludesTax() {
        var item = new OrderItemRequest(
            null, null, "Item", "SKU003",
            new BigDecimal("100.00"), BigDecimal.ONE,
            BigDecimal.ZERO, null, null
        );
        PricingResult result = pricingEngine.calculate(List.of(item), BigDecimal.ZERO, false);
        // tax = 100 * 0.05 = 5, grandTotal = 105 (rounded)
        assertThat(result.taxTotal()).isEqualByComparingTo("5.00");
        assertThat(result.grandTotal()).isEqualByComparingTo("105.00");
    }

    // ========================================
    // 含稅拆算 / Tax inclusive extraction
    // ========================================
    @Test
    void calculate_taxInclusive_extractsCorrectTax() {
        var item = new OrderItemRequest(
            null, null, "Item", "SKU004",
            new BigDecimal("105.00"), BigDecimal.ONE,
            BigDecimal.ZERO, null, null
        );
        PricingResult result = pricingEngine.calculate(List.of(item), BigDecimal.ZERO, true);
        // tax = 105 * 0.05 / 1.05 = 5.00
        assertThat(result.taxTotal()).isEqualByComparingTo("5.00");
        assertThat(result.grandTotal()).isEqualByComparingTo("105.00");
    }

    // ========================================
    // 折扣計算 / Discount calculation
    // ========================================
    @Test
    void calculate_withDiscount_reducesGrandTotal() {
        var item = new OrderItemRequest(
            null, null, "Item", "SKU005",
            new BigDecimal("200.00"), BigDecimal.ONE,
            BigDecimal.ZERO, null, null
        );
        PricingResult result = pricingEngine.calculate(List.of(item), new BigDecimal("20.00"), false);
        assertThat(result.discountTotal()).isEqualByComparingTo("20.00");
        // afterDiscount = 180, tax = 9, grandTotal = 189
        assertThat(result.grandTotal()).isEqualByComparingTo("189.00");
    }

    // ========================================
    // 找零計算 / Change calculation
    // ========================================
    @Test
    void calculateChange_exactPayment_returnsZero() {
        BigDecimal change = pricingEngine.calculateChange(new BigDecimal("100"), new BigDecimal("100"));
        assertThat(change).isEqualByComparingTo("0.00");
    }

    @Test
    void calculateChange_overpayment_returnsCorrectChange() {
        BigDecimal change = pricingEngine.calculateChange(new BigDecimal("200"), new BigDecimal("157.50"));
        assertThat(change).isEqualByComparingTo("42.50");
    }

    @Test
    void calculateChange_nullTendered_returnsZero() {
        BigDecimal change = pricingEngine.calculateChange(null, new BigDecimal("100"));
        assertThat(change).isEqualByComparingTo("0.00");
    }
}
