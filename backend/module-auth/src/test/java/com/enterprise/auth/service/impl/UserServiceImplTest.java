package com.enterprise.auth.service.impl;

import com.enterprise.auth.dto.RegisterRequest;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.repository.UserRepository;
import com.enterprise.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFailedAttempts(0);
    }

    @Test
    void testCreateUser_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("password123");
        
        when(userRepository.findByUsername(req.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pw");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.createUser(req);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("hashed_pw", result.getPasswordHash());
        assertEquals("ACTIVE", result.getStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_Conflict() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");

        when(userRepository.findByUsername(req.getUsername())).thenReturn(Optional.of(testUser));

        assertThrows(BusinessException.class, () -> userService.createUser(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testHandleLoginFailure_LockUser() {
        testUser.setFailedAttempts(4); // Next failure will lock
        
        userService.handleLoginFailure(testUser);

        assertEquals(5, testUser.getFailedAttempts());
        assertNotNull(testUser.getLockedUntil());
        assertTrue(testUser.getLockedUntil().isAfter(LocalDateTime.now()));
        verify(userRepository).save(testUser);
    }
}
