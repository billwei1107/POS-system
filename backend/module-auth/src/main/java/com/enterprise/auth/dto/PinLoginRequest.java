package com.enterprise.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * @file PinLoginRequest.java
 * @description POS PIN 碼登入請求 / PIN login request for POS terminal
 * @description_en Accepts either a terminal UUID or a stable terminal code for POS PIN login
 * @description_zh 支援使用終端 UUID 或穩定終端代碼進行 POS PIN 登入
 */
@Data
public class PinLoginRequest {

    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must be 4-6 digits")
    private String pin;

    private UUID terminalId;

    @Size(max = 50, message = "Terminal code must be 50 characters or fewer")
    private String terminalCode;

    @AssertTrue(message = "Terminal ID or terminal code is required")
    public Boolean getTerminalIdentifierPresent() {
        return terminalId != null || (terminalCode != null && !terminalCode.isBlank());
    }
}
