package com.enterprise.auth.service.impl;

import com.enterprise.auth.dto.*;
import com.enterprise.auth.entity.PinCode;
import com.enterprise.auth.entity.TerminalToken;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.event.ManagerOverrideEvent;
import com.enterprise.auth.event.UserSwitchedEvent;
import com.enterprise.auth.repository.PinCodeRepository;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.repository.TerminalTokenRepository;
import com.enterprise.auth.repository.UserRepository;
import com.enterprise.auth.service.PosAuthService;
import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * POS 認證服務實作 / POS authentication service implementation
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pos.auth.pin-login", havingValue = "true")
public class PosAuthServiceImpl implements PosAuthService {

    private final PinCodeRepository pinCodeRepository;
    private final TerminalTokenRepository terminalTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 檢查用戶是否擁有指定角色 / Check if user has a specific role
     */
    private boolean userRoleExists(UUID userId, UUID roleId) {
        return userRepository.findById(userId)
                .map(u -> roleRepository.findById(roleId).isPresent())
                .orElse(false);
    }

    @Override
    @Transactional
    public PinLoginResponse pinLogin(PinLoginRequest request) {
        // 取得所有啟用中的 PIN 碼 / Get all active PIN codes
        List<PinCode> activePins = pinCodeRepository.findAll().stream()
                .filter(p -> p.getActive() && p.getDeletedAt() == null)
                .toList();

        for (PinCode pinCode : activePins) {
            // 檢查鎖定狀態 / Check lockout
            if (pinCode.getLockedUntil() != null && pinCode.getLockedUntil().isAfter(LocalDateTime.now())) {
                continue;
            }

            if (passwordEncoder.matches(request.getPin(), pinCode.getPinHash())) {
                // PIN 匹配成功 / PIN matched
                User user = userRepository.findById(pinCode.getUserId())
                        .orElseThrow(() -> new BusinessException(404, "User not found"));

                if (!"ACTIVE".equals(user.getStatus())) {
                    throw new BusinessException(403, "User account is not active");
                }

                // 重置失敗次數 / Reset failed attempts
                pinCode.setFailedAttempts(0);
                pinCode.setLastUsedAt(LocalDateTime.now());
                pinCode.setLockedUntil(null);
                pinCodeRepository.save(pinCode);

                // 產生 JWT / Generate JWT token
                String token = jwtTokenProvider.generateToken(user.getId(), "CASHIER");

                return PinLoginResponse.builder()
                        .token(token)
                        .refreshToken(token)
                        .userId(user.getId().toString())
                        .username(user.getUsername())
                        .terminalId(request.getTerminalId().toString())
                        .build();
            }
        }

        // 所有 PIN 都不匹配，記錄失敗 / No PIN matched
        throw new BusinessException(401, "Invalid PIN");
    }

    @Override
    @Transactional
    public void setPin(UUID userId, SetPinRequest request) {
        // 檢查使用者是否存在 / Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "User not found"));

        // 查找或建立 PIN 碼紀錄 / Find or create PIN record
        PinCode pinCode = pinCodeRepository
                .findByUserIdAndTerminalTypeAndActiveTrue(userId, request.getTerminalType())
                .orElse(new PinCode());

        pinCode.setUserId(userId);
        pinCode.setPinHash(passwordEncoder.encode(request.getPin()));
        pinCode.setTerminalType(request.getTerminalType());
        pinCode.setActive(true);
        pinCode.setFailedAttempts(0);
        pinCode.setLockedUntil(null);

        pinCodeRepository.save(pinCode);
    }

    @Override
    @Transactional
    public PinLoginResponse quickSwitch(PinLoginRequest request) {
        // 快速切換本質上等同 PIN 登入 / Quick switch is essentially a PIN login
        PinLoginResponse response = pinLogin(request);

        // 發布切換事件 / Publish user switched event
        eventPublisher.publishEvent(new UserSwitchedEvent(
                this, null, UUID.fromString(response.getUserId()), request.getTerminalId()));

        return response;
    }

    @Override
    @Transactional
    public boolean managerOverride(ManagerOverrideRequest request) {
        // 嘗試以 PIN 或密碼驗證管理者身份 / Verify manager identity via PIN or password
        User manager = null;

        if (request.getPin() != null) {
            // 透過 PIN 驗證 / Verify via PIN
            List<PinCode> activePins = pinCodeRepository.findAll().stream()
                    .filter(p -> p.getActive() && p.getDeletedAt() == null)
                    .toList();

            for (PinCode pinCode : activePins) {
                if (passwordEncoder.matches(request.getPin(), pinCode.getPinHash())) {
                    manager = userRepository.findById(pinCode.getUserId()).orElse(null);
                    break;
                }
            }
        } else if (request.getPassword() != null) {
            // 透過密碼驗證 / Verify via password (iterate users - not efficient but functional)
            List<User> users = userRepository.findAll();
            for (User user : users) {
                if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                    manager = user;
                    break;
                }
            }
        }

        if (manager == null) {
            throw new BusinessException(401, "Invalid manager credentials");
        }

        // ========================================
        // 管理者角色驗證 / Manager Role Verification
        // ========================================
        final User verifiedManager = manager;
        List<String> managerRoleCodes = List.of("STORE_MANAGER", "SHIFT_MANAGER");
        boolean isManager = roleRepository.findAll().stream()
                .filter(role -> managerRoleCodes.contains(role.getCode()))
                .anyMatch(role -> userRoleExists(verifiedManager.getId(), role.getId()));
        if (!isManager) {
            throw new BusinessException(403, "User does not have manager privileges");
        }

        // 發布覆蓋事件 / Publish override event
        eventPublisher.publishEvent(new ManagerOverrideEvent(
                this, manager.getId(), request.getAction(), request.getTargetOrderId()));

        return true;
    }

    @Override
    @Transactional
    public String registerTerminal(TerminalRegisterRequest request) {
        // 停用舊 Token / Deactivate existing token
        terminalTokenRepository.findByTerminalIdAndActiveTrue(request.getTerminalId())
                .ifPresent(existing -> {
                    existing.setActive(false);
                    terminalTokenRepository.save(existing);
                });

        // 產生新 Token / Generate new token
        String rawToken = UUID.randomUUID().toString();

        TerminalToken terminalToken = new TerminalToken();
        terminalToken.setTerminalId(request.getTerminalId());
        terminalToken.setStoreId(request.getStoreId());
        terminalToken.setTokenHash(passwordEncoder.encode(rawToken));
        terminalToken.setDeviceFingerprint(request.getDeviceFingerprint());
        terminalToken.setActive(true);
        terminalToken.setRegisteredAt(LocalDateTime.now());

        terminalTokenRepository.save(terminalToken);

        // 僅回傳一次原始 Token / Return raw token once (client must store it)
        return rawToken;
    }

    @Override
    public boolean validateTerminalToken(UUID terminalId, String token) {
        return terminalTokenRepository.findByTerminalIdAndActiveTrue(terminalId)
                .map(tt -> {
                    boolean valid = passwordEncoder.matches(token, tt.getTokenHash());
                    if (valid) {
                        tt.setLastActiveAt(LocalDateTime.now());
                        terminalTokenRepository.save(tt);
                    }
                    return valid;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public void deactivatePin(UUID userId, String terminalType) {
        pinCodeRepository.findByUserIdAndTerminalTypeAndActiveTrue(userId, terminalType)
                .ifPresent(pinCode -> {
                    pinCode.setActive(false);
                    pinCodeRepository.save(pinCode);
                });
    }
}
