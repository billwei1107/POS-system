package com.enterprise.auth.controller;

import com.enterprise.auth.dto.*;
import com.enterprise.auth.service.PosAuthService;
import com.enterprise.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * POS 認證控制器 / POS authentication controller
 */
@RestController
@RequestMapping("/api/v1/pos/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pos.auth.pin-login", havingValue = "true")
public class PosAuthController {

    private final PosAuthService posAuthService;

    @PostMapping("/pin-login")
    public ApiResponse<PinLoginResponse> pinLogin(@Valid @RequestBody PinLoginRequest request) {
        return ApiResponse.success(posAuthService.pinLogin(request));
    }

    @PostMapping("/quick-switch")
    public ApiResponse<PinLoginResponse> quickSwitch(@Valid @RequestBody PinLoginRequest request) {
        return ApiResponse.success(posAuthService.quickSwitch(request));
    }

    @PostMapping("/pin")
    public ApiResponse<Void> setPin(@RequestParam UUID userId, @Valid @RequestBody SetPinRequest request) {
        posAuthService.setPin(userId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/pin")
    public ApiResponse<Void> deactivatePin(@RequestParam UUID userId,
                                            @RequestParam(defaultValue = "POS") String terminalType) {
        posAuthService.deactivatePin(userId, terminalType);
        return ApiResponse.success(null);
    }

    @PostMapping("/manager-override")
    public ApiResponse<Boolean> managerOverride(@Valid @RequestBody ManagerOverrideRequest request) {
        return ApiResponse.success(posAuthService.managerOverride(request));
    }

    @PostMapping("/terminal/register")
    public ApiResponse<String> registerTerminal(@Valid @RequestBody TerminalRegisterRequest request) {
        return ApiResponse.success(posAuthService.registerTerminal(request));
    }

    @PostMapping("/terminal/validate")
    public ApiResponse<Boolean> validateTerminal(@RequestParam UUID terminalId, @RequestParam String token) {
        return ApiResponse.success(posAuthService.validateTerminalToken(terminalId, token));
    }
}
