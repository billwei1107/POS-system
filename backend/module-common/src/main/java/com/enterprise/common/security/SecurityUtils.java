package com.enterprise.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    /**
     * 從 Spring Security Context 中取得目前 JWT 角色
     * Resolve current JWT role from Spring Security authorities.
     */
    public static String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return null;
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .findFirst()
                .orElseGet(() -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(authority -> authority.startsWith("ROLE_"))
                        .map(authority -> authority.substring("ROLE_".length()))
                        .findFirst()
                        .orElse(null));
    }
}
