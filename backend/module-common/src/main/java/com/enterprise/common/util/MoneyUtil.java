package com.enterprise.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @file MoneyUtil.java
 * @description 金額計算工具類 / Money utility
 */
public class MoneyUtil {
    public static BigDecimal round(BigDecimal amount) {
        return amount != null ? amount.setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
}
