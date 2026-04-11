package com.enterprise.attendance.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @file AttendanceModuleConfig.java
 * @description 打卡考勤模組配置 / Attendance module configuration
 * @description_zh 解耦 JPA 掃描路徑，確保模塊可獨立裝卸
 */
@Configuration
@ComponentScan(basePackages = "com.enterprise.attendance")
@EntityScan(basePackages = "com.enterprise.attendance.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.attendance.repository")
public class AttendanceModuleConfig {
}
