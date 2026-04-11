package com.enterprise.common.feature;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * @file ModuleCondition.java
 * @description ConditionalOnModule 的條件判斷類 / Logic evaluating feature flags
 */
public class ModuleCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnModule.class.getName());
        if (attributes != null && attributes.containsKey("value")) {
            String moduleName = (String) attributes.get("value");
            String property = context.getEnvironment().getProperty("modules." + moduleName);
            return "true".equalsIgnoreCase(property);
        }
        return false; // 預設關閉或由其它方式設定
    }
}
