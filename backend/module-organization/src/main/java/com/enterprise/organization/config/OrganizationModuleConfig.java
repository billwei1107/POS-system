package com.enterprise.organization.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.enterprise.organization")
@EntityScan(basePackages = "com.enterprise.organization.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.organization.repository")
@ConditionalOnProperty(name = "modules.organization", havingValue = "true", matchIfMissing = true)
public class OrganizationModuleConfig {
}
