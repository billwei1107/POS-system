/**
 * @file CoreModuleConfig.java
 * @description POS Core 模組配置 / POS Core module configuration
 * @description_en Activates pos-core module beans via feature toggle; enabled by default
 * @description_zh 透過 Feature Toggle 啟用 pos-core 模組 Bean，預設開啟
 */
package com.enterprise.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "modules.pos-core", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.enterprise.core")
@EntityScan(basePackages = "com.enterprise.core.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.core.repository")
public class CoreModuleConfig {
}
