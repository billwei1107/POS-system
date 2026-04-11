package com.enterprise.auth.service.impl;

import com.enterprise.auth.dto.LoginRequest;
import com.enterprise.auth.dto.LoginResponse;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.repository.UserRepository;
import com.enterprise.auth.service.AuthService;
import com.enterprise.auth.service.UserService;
import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthServiceImpl(UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "Invalid username or password"));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(403, "User account is suspended");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(403, "User account is locked. Please try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            userService.handleLoginFailure(user);
            throw new BusinessException(401, "Invalid username or password");
        }

        userService.handleLoginSuccess(user);

        String token = jwtTokenProvider.generateToken(user.getId(), "USER");

        return LoginResponse.builder()
                .token(token)
                .refreshToken(token)
                .userId(user.getId().toString())
                .username(user.getUsername())
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        throw new BusinessException(400, "Refresh Token flow not implemented yet");
    }

    @Override
    public void logout(String userId) {
        // Implement token revocation listing
    }
}
