/**
 * @file ProductModuleConfig.java
 * @description 商品模組自動配置 / Product module auto-configuration
 * @description_en Activates when modules.product=true (default: true for POS)
 * @description_zh 當 modules.product=true 時啟用，預設啟用
 */
package com.enterprise.product.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.enterprise.product")
@EntityScan(basePackages = "com.enterprise.product.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.product.repository")
@ConditionalOnProperty(name = "modules.product", havingValue = "true", matchIfMissing = true)
public class ProductModuleConfig {
}
