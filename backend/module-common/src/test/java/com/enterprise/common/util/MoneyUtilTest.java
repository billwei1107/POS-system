package com.enterprise.common.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class MoneyUtilTest {
    @Test
    public void testRound() {
        BigDecimal amount = new BigDecimal("100.12345");
        BigDecimal rounded = MoneyUtil.round(amount);
        assertEquals(new BigDecimal("100.1235"), rounded);

        assertEquals(BigDecimal.ZERO, MoneyUtil.round(null));
    }
}
