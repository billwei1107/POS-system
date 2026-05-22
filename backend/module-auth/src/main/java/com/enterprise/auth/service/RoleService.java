package com.enterprise.auth.service;

import com.enterprise.auth.dto.CreateRoleRequest;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.dto.RolePermissionSummaryResponse;
import com.enterprise.auth.dto.UpdateRolePermissionsRequest;
import java.util.List;
import java.util.UUID;

public interface RoleService {
    Role getRoleById(UUID id);
    List<Role> getAllRoles();
    List<RolePermissionSummaryResponse> getRolePermissionSummaries();
    RolePermissionSummaryResponse createRole(CreateRoleRequest request);
    RolePermissionSummaryResponse updateRolePermissions(UUID roleId, UpdateRolePermissionsRequest request);
}
