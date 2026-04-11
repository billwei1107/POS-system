package com.enterprise.common.util;

/**
 * @file StringUtil.java
 * @description 字串工具類 / String utility
 */
public class StringUtil {
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
