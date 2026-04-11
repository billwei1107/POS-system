package com.enterprise.auth.service.impl;

import com.enterprise.auth.dto.LoginRequest;
import com.enterprise.auth.dto.LoginResponse;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.repository.UserRepository;
import com.enterprise.auth.service.UserService;
import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest request;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hashed_pw");
        testUser.setStatus("ACTIVE");

        request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser.getId(), "USER")).thenReturn("dummy-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("dummy-token", response.getToken());
        verify(userService).handleLoginSuccess(testUser);
    }

    @Test
    void testLogin_UserSuspended() {
        testUser.setStatus("INACTIVE");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));

        assertThrows(BusinessException.class, () -> authService.login(request));
        verify(userService, never()).handleLoginSuccess(any());
    }

    @Test
    void testLogin_UserLocked() {
        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));

        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.login(request));
        verify(userService).handleLoginFailure(testUser);
    }
}
