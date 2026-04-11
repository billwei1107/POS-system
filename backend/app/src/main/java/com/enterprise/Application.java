package com.enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @file Application.java
 * @description 後端應用程式進入點 / Application Entry Point
 * @description_en Main Spring Boot application class
 * @description_zh 負責啟動整個 Spring Boot 後端微模塊系統
 */
@SpringBootApplication(scanBasePackages = "com.enterprise")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
