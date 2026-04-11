package com.enterprise.common.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilTest {
    @Test
    public void testFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 7, 13, 0, 0);
        String formatted = DateUtil.format(dateTime);
        assertEquals("2026-04-07 13:00:00", formatted);

        assertNull(DateUtil.format(null));
    }
}
