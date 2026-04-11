package com.enterprise.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

/**
 * POS PIN 碼登入請求 / PIN login request for POS terminal
 */
@Data
public class PinLoginRequest {

    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must be 4-6 digits")
    private String pin;

    @NotNull(message = "Terminal ID is required")
    private UUID terminalId;
}
