package com.enterprise.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

/**
 * 店長權限覆蓋請求 / Manager override authorization request
 */
@Data
public class ManagerOverrideRequest {

    private String pin;

    private String password;

    @NotBlank(message = "Action is required")
    private String action;

    private UUID targetOrderId;

    private String reason;
}
