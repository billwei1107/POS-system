/**
 * @file PaymentModuleConfig.java
 * @description 支付模組配置 / Payment module configuration
 * @description_en Feature toggle for payment module; enables component scan, entity scan, JPA repositories
 * @description_zh 支付模組的 Feature Toggle，控制 Bean 掃描、Entity 掃描與 JPA Repository 啟用
 */
package com.enterprise.payment.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "modules.pos-payment", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.enterprise.payment")
@EntityScan(basePackages = "com.enterprise.payment.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.payment.repository")
public class PaymentModuleConfig {
}
