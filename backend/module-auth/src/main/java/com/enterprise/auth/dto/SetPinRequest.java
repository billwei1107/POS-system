package com.enterprise.auth.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 設定 PIN 碼請求 / Set PIN request (userId from JWT context)
 */
@Data
public class SetPinRequest {

    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must be 4-6 digits")
    private String pin;

    private String terminalType = "POS";
}
