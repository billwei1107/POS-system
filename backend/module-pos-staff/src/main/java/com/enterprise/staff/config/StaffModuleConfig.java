/**
 * @file StaffModuleConfig.java
 * @description 排班模組配置 / Staff module configuration
 * @description_en Feature toggle configuration for the pos-staff module
 * @description_zh pos-staff 模組的 Feature Toggle 配置
 */
package com.enterprise.staff.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "modules.pos-staff", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.enterprise.staff")
@EntityScan(basePackages = "com.enterprise.staff.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.staff.repository")
public class StaffModuleConfig {
}
