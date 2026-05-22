package com.enterprise.auth.service.impl;

import com.enterprise.auth.dto.CreateUserRequest;
import com.enterprise.auth.dto.RegisterRequest;
import com.enterprise.auth.dto.ResetUserPasswordRequest;
import com.enterprise.auth.dto.UpdateUserRolesRequest;
import com.enterprise.auth.dto.UpdateUserStatusRequest;
import com.enterprise.auth.dto.UserRoleSummaryResponse;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.entity.UserRole;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.repository.UserRepository;
import com.enterprise.auth.repository.UserRoleRepository;
import com.enterprise.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role cashierRole;
    private Role managerRole;
    private Role superAdminRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setFailedAttempts(0);

        cashierRole = role("CASHIER");
        managerRole = role("STORE_MANAGER");
        superAdminRole = role("SUPER_ADMIN");
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

    @Test
    void updateUserRolesReplacesMappingsAndReturnsSafeSummary() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(roleRepository.findAllById(any())).thenReturn(List.of(managerRole, cashierRole));
        when(roleRepository.findByCode("SUPER_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRoleRepository.existsByUserIdAndRoleId(testUser.getId(), superAdminRole.getId())).thenReturn(false);
        when(roleRepository.findAll()).thenReturn(List.of(cashierRole, managerRole, superAdminRole));
        when(userRoleRepository.findAllByUserId(testUser.getId())).thenReturn(List.of(
                userRole(testUser.getId(), cashierRole.getId()),
                userRole(testUser.getId(), managerRole.getId())
        ));

        UserRoleSummaryResponse response = userService.updateUserRoles(
                testUser.getId(),
                new UpdateUserRolesRequest(List.of(managerRole.getId(), cashierRole.getId(), cashierRole.getId()))
        );

        assertEquals("testuser", response.user().username());
        assertEquals(List.of("CASHIER", "STORE_MANAGER"), response.roles().stream().map(role -> role.code()).toList());
        verify(userRoleRepository).deleteAllByUserId(testUser.getId());
        verify(userRoleRepository, times(2)).save(any(UserRole.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchUserRoleSummariesReturnsPagedSafeSummaries() {
        testUser.setStatus("ACTIVE");
        testUser.setEmail("test@example.local");
        when(roleRepository.findAll()).thenReturn(List.of(cashierRole));
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1));
        when(userRoleRepository.findAllByUserId(testUser.getId())).thenReturn(List.of(
                userRole(testUser.getId(), cashierRole.getId())
        ));

        var response = userService.searchUserRoleSummaries("test", "ACTIVE", cashierRole.getId(), PageRequest.of(0, 10));

        assertEquals(1, response.getTotalElements());
        assertEquals("testuser", response.getContent().getFirst().user().username());
        assertEquals(List.of("CASHIER"), response.getContent().getFirst().roles().stream().map(role -> role.code()).toList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchUserRoleSummariesAcceptsLockedStatusFilter() {
        testUser.setStatus("ACTIVE");
        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(20));
        when(roleRepository.findAll()).thenReturn(List.of(cashierRole));
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1));
        when(userRoleRepository.findAllByUserId(testUser.getId())).thenReturn(List.of());

        var response = userService.searchUserRoleSummaries("", "LOCKED", null, PageRequest.of(0, 10));

        assertEquals(1, response.getTotalElements());
        assertEquals(testUser.getLockedUntil(), response.getContent().getFirst().user().lockedUntil());
        verify(userRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void updateUserRolesRejectsMissingRoleAndDoesNotDeleteMappings() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(roleRepository.findAllById(any())).thenReturn(List.of(cashierRole));

        assertThrows(BusinessException.class, () -> userService.updateUserRoles(
                testUser.getId(),
                new UpdateUserRolesRequest(List.of(cashierRole.getId(), managerRole.getId()))
        ));

        verify(userRoleRepository, never()).deleteAllByUserId(any());
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void updateUserRolesRejectsRemovingLastSuperAdmin() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(roleRepository.findAllById(any())).thenReturn(List.of(cashierRole));
        when(roleRepository.findByCode("SUPER_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRoleRepository.existsByUserIdAndRoleId(testUser.getId(), superAdminRole.getId())).thenReturn(true);
        when(userRoleRepository.findAllByRoleId(superAdminRole.getId())).thenReturn(List.of(
                userRole(testUser.getId(), superAdminRole.getId())
        ));

        assertThrows(BusinessException.class, () -> userService.updateUserRoles(
                testUser.getId(),
                new UpdateUserRolesRequest(List.of(cashierRole.getId()))
        ));

        verify(userRoleRepository, never()).deleteAllByUserId(any());
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void createManagedUserCreatesAccountAndInitialRoles() {
        when(userRepository.findByUsername("newmanager")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("hashed_secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(roleRepository.findAllById(any())).thenReturn(List.of(managerRole));
        when(roleRepository.findByCode("SUPER_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRoleRepository.existsByUserIdAndRoleId(any(), eq(superAdminRole.getId()))).thenReturn(false);
        when(roleRepository.findAll()).thenReturn(List.of(managerRole, superAdminRole));
        when(userRoleRepository.findAllByUserId(any())).thenAnswer(invocation -> List.of(
                userRole(invocation.getArgument(0), managerRole.getId())
        ));

        UserRoleSummaryResponse response = userService.createManagedUser(new CreateUserRequest(
                "newmanager",
                "secret123",
                "manager@example.local",
                "0912345678",
                List.of(managerRole.getId())
        ));

        assertEquals("newmanager", response.user().username());
        assertEquals("manager@example.local", response.user().email());
        assertEquals(List.of("STORE_MANAGER"), response.roles().stream().map(role -> role.code()).toList());
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void updateUserStatusRejectsDisablingLastActiveSuperAdmin() {
        testUser.setStatus("ACTIVE");
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(roleRepository.findByCode("SUPER_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRoleRepository.existsByUserIdAndRoleId(testUser.getId(), superAdminRole.getId())).thenReturn(true);
        when(userRoleRepository.findAllByRoleId(superAdminRole.getId())).thenReturn(List.of(
                userRole(testUser.getId(), superAdminRole.getId())
        ));

        assertThrows(BusinessException.class, () -> userService.updateUserStatus(
                testUser.getId(),
                new UpdateUserStatusRequest("INACTIVE")
        ));

        verify(userRepository, never()).save(testUser);
    }

    @Test
    void resetPasswordClearsLockAndFailedAttempts() {
        testUser.setFailedAttempts(5);
        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("hashed_new");
        when(roleRepository.findAll()).thenReturn(List.of(cashierRole));
        when(userRoleRepository.findAllByUserId(testUser.getId())).thenReturn(List.of());

        UserRoleSummaryResponse response = userService.resetPassword(
                testUser.getId(),
                new ResetUserPasswordRequest("newPassword123")
        );

        assertEquals("hashed_new", testUser.getPasswordHash());
        assertEquals(0, testUser.getFailedAttempts());
        assertNull(testUser.getLockedUntil());
        assertEquals("testuser", response.user().username());
        verify(userRepository).save(testUser);
    }

    private Role role(String code) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode(code);
        role.setName(code);
        role.setDescription(code + " role");
        return role;
    }

    private UserRole userRole(UUID userId, UUID roleId) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return userRole;
    }
}
