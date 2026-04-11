package com.enterprise.common.feature;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * @file ConditionalOnModule.java
 * @description 根據 application.yml 決定模塊是否啟用的注解 / Conditional toggle annotation
 *              for modules
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ModuleCondition.class)
public @interface ConditionalOnModule {
    String value();
}
