package com.enterprise.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POS PIN 碼登入回應 / PIN login response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinLoginResponse {
    private String token;
    private String refreshToken;
    private String userId;
    private String username;
    private String storeId;
    private String terminalId;
    private String role;
}
