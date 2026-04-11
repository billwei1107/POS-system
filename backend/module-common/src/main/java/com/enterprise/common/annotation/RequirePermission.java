package com.enterprise.common.annotation;

import java.lang.annotation.*;

/**
 * @file RequirePermission.java
 * @description 需要特定權限才能訪問的方法注解 / Requires a specific permission code to access
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    String value();
}
