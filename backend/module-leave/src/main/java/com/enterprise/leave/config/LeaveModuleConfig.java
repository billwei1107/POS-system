/**
 * @file LeaveModuleConfig.java
 * @description 請假模塊配置 / Leave module configuration
 * @description_en Configures JPA entity scan and repository scan for the leave module
 * @description_zh 配置請假模塊的 JPA Entity 與 Repository 掃描路徑
 */
package com.enterprise.leave.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.enterprise.leave")
@EntityScan(basePackages = "com.enterprise.leave.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.leave.repository")
public class LeaveModuleConfig {
}
