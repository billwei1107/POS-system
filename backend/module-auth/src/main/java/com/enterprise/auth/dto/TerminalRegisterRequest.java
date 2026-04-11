package com.enterprise.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * 終端機註冊請求 / Terminal registration request
 */
@Data
public class TerminalRegisterRequest {

    @NotNull(message = "Terminal ID is required")
    private UUID terminalId;

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    private String deviceFingerprint;
}
