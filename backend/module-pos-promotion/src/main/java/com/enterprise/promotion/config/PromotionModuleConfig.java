/**
 * @file PromotionModuleConfig.java
 * @description POS 促銷模組設定 / POS promotion module configuration
 * @description_en Enables promotion beans when the POS promotion module flag is active
 * @description_zh 依模組開關啟用 POS 促銷相關 Bean
 */
package com.enterprise.promotion.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "modules.pos-promotion", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.enterprise.promotion")
@EntityScan(basePackages = "com.enterprise.promotion.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.promotion.repository")
public class PromotionModuleConfig {
}
