package com.enterprise.workflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(prefix = "modules", name = "workflow", havingValue = "true", matchIfMissing = true)
@EntityScan(basePackages = "com.enterprise.workflow.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.workflow.repository")
public class WorkflowModuleConfig {
}
