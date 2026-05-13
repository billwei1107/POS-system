/**
 * @file InventoryModuleConfig.java
 * @description 庫存模組配置 / Inventory module configuration
 * @description_en Spring configuration class with ConditionalOnProperty Feature Toggle for pos-inventory
 * @description_zh 庫存模組的 Spring 配置，實作 Feature Toggle 條件載入
 */
package com.enterprise.inventory.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "modules.pos-inventory", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.enterprise.inventory")
@EntityScan(basePackages = "com.enterprise.inventory.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.inventory.repository")
public class InventoryModuleConfig {
}
