package com.enterprise.common.annotation;

import java.lang.annotation.*;

/**
 * @file Auditable.java
 * @description AOP 日誌記錄切面注解 / Annotation for audit logging
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String action() default "";

    String module() default "";
}
