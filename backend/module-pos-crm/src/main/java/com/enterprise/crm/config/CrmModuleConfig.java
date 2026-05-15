/**
 * @file CrmModuleConfig.java
 * @description CRM 模組配置 / CRM module configuration
 * @description_en Feature toggle configuration for the POS CRM module
 * @description_zh POS CRM 模組的 Feature Toggle 配置，啟用會員與點數資料存取
 */
package com.enterprise.crm.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "modules.pos-crm", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.enterprise.crm")
@EntityScan(basePackages = "com.enterprise.crm.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.crm.repository")
public class CrmModuleConfig {
}
