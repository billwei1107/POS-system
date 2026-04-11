package com.enterprise.auth.service;

import com.enterprise.auth.dto.LoginRequest;
import com.enterprise.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    void logout(String userId);
}
