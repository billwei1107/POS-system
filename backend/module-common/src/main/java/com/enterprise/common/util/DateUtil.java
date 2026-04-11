package com.enterprise.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @file DateUtil.java
 * @description 日期時間工具類 / Date time utility
 */
public class DateUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }
}
