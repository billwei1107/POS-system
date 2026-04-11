package com.enterprise.auth.service;

import com.enterprise.auth.dto.*;

import java.util.UUID;

/**
 * POS 認證服務介面 / POS authentication service interface
 */
public interface PosAuthService {

    PinLoginResponse pinLogin(PinLoginRequest request);

    void setPin(UUID userId, SetPinRequest request);

    PinLoginResponse quickSwitch(PinLoginRequest request);

    boolean managerOverride(ManagerOverrideRequest request);

    String registerTerminal(TerminalRegisterRequest request);

    boolean validateTerminalToken(UUID terminalId, String token);

    void deactivatePin(UUID userId, String terminalType);
}
