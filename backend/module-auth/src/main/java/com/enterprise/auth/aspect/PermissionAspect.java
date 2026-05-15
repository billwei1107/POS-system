package com.enterprise.auth.aspect;

import com.enterprise.auth.service.PermissionGuardService;
import com.enterprise.common.annotation.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @file PermissionAspect.java
 * @description 權限註解切面 / Permission annotation aspect
 * @description_en Enforces @RequirePermission before sensitive backend operations
 * @description_zh 在敏感後端操作執行前檢查 @RequirePermission 權限代碼
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionGuardService permissionGuardService;

    @Before("@annotation(requirePermission)")
    public void enforcePermission(RequirePermission requirePermission) {
        permissionGuardService.requirePermission(requirePermission.value());
    }
}
