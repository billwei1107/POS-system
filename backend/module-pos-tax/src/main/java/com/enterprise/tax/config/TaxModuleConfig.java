/**
 * @file TaxModuleConfig.java
 * @description 稅務模組配置 / Tax module configuration
 * @description_en Feature toggle and component scan for module-pos-tax; disabled via modules.pos-tax=false
 * @description_zh 稅務模組的 Feature Toggle 與組件掃描設定
 */
package com.enterprise.tax.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "modules.pos-tax", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.enterprise.tax")
@EntityScan(basePackages = "com.enterprise.tax.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.tax.repository")
public class TaxModuleConfig {
}
