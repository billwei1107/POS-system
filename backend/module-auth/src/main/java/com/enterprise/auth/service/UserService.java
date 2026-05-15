package com.enterprise.auth.service;

import com.enterprise.auth.dto.CreateUserRequest;
import com.enterprise.auth.dto.RegisterRequest;
import com.enterprise.auth.dto.ResetUserPasswordRequest;
import com.enterprise.auth.dto.UpdateUserRolesRequest;
import com.enterprise.auth.dto.UpdateUserStatusRequest;
import com.enterprise.auth.dto.UserRoleSummaryResponse;
import com.enterprise.auth.entity.User;
import com.enterprise.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User createUser(RegisterRequest request);
    UserRoleSummaryResponse createManagedUser(CreateUserRequest request);
    User getUserById(UUID id);
    List<UserRoleSummaryResponse> getUserRoleSummaries();
    PageResponse<UserRoleSummaryResponse> searchUserRoleSummaries(String keyword, String status, UUID roleId, Pageable pageable);
    UserRoleSummaryResponse getUserRoleSummary(UUID id);
    UserRoleSummaryResponse updateUserRoles(UUID userId, UpdateUserRolesRequest request);
    UserRoleSummaryResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request);
    UserRoleSummaryResponse resetPassword(UUID userId, ResetUserPasswordRequest request);
    void handleLoginSuccess(User user);
    void handleLoginFailure(User user);
}
