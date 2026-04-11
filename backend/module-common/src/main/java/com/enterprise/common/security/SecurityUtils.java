package com.enterprise.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * 安全性工具類別
 * 提供跨模組提取目前登入使用者資訊之靜態方法
 */
public class SecurityUtils {

    /**
     * 從 Spring Security Context 中獲取當下請求的使用者 ID
     * JwtAuthenticationFilter 設定 Principal 為 UUID 物件
     */
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UUID) {
                return principal.toString();
            }
            if (principal instanceof String) {
                return (String) principal;
            }
        }
        return null;
    }
}
