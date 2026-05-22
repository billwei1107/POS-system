package com.enterprise.auth.service.impl;

import com.enterprise.auth.dto.CreateRoleRequest;
import com.enterprise.auth.dto.PermissionResponse;
import com.enterprise.auth.dto.RolePermissionSummaryResponse;
import com.enterprise.auth.dto.UpdateRolePermissionsRequest;
import com.enterprise.auth.entity.Permission;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.entity.RolePermission;
import com.enterprise.auth.repository.PermissionRepository;
import com.enterprise.auth.repository.RolePermissionRepository;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.service.RoleService;
import com.enterprise.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    public Role getRoleById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Role not found"));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public List<RolePermissionSummaryResponse> getRolePermissionSummaries() {
        return roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::getCode))
                .map(this::toRolePermissionSummary)
                .toList();
    }

    @Override
    @Transactional
    public RolePermissionSummaryResponse createRole(CreateRoleRequest request) {
        String code = normalizeCode(request.code());
        if (roleRepository.findByCode(code).isPresent()) {
            throw new BusinessException(400, "Role code already exists");
        }

        Role role = new Role();
        role.setName(request.name().trim());
        role.setCode(code);
        role.setDescription(request.description() == null ? null : request.description().trim());
        Role savedRole = roleRepository.save(role);

        List<UUID> permissionIds = request.permissionIds() == null ? List.of() : request.permissionIds();
        replaceRolePermissions(savedRole, permissionIds);
        return toRolePermissionSummary(savedRole);
    }

    @Override
    @Transactional
    public RolePermissionSummaryResponse updateRolePermissions(UUID roleId, UpdateRolePermissionsRequest request) {
        Role role = getRoleById(roleId);
        if ("SUPER_ADMIN".equals(role.getCode())) {
            throw new BusinessException(400, "SUPER_ADMIN permissions are managed by system policy");
        }

        List<UUID> permissionIds = request == null || request.permissionIds() == null
                ? List.of()
                : request.permissionIds();
        replaceRolePermissions(role, permissionIds);

        return toRolePermissionSummary(role);
    }

    private void replaceRolePermissions(Role role, List<UUID> permissionIds) {
        Set<UUID> uniquePermissionIds = new LinkedHashSet<>(permissionIds);

        List<Permission> permissions = permissionRepository.findAllById(uniquePermissionIds);
        if (permissions.size() != uniquePermissionIds.size()) {
            throw new BusinessException(400, "One or more permissions do not exist");
        }

        rolePermissionRepository.deleteAllByRoleId(role.getId());
        permissions.stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .forEach(permission -> {
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRoleId(role.getId());
                    rolePermission.setPermissionId(permission.getId());
                    rolePermissionRepository.save(rolePermission);
                });
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private RolePermissionSummaryResponse toRolePermissionSummary(Role role) {
        Map<UUID, Permission> permissionsById = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getId, permission -> permission));

        List<PermissionResponse> permissions = rolePermissionRepository.findAllByRoleId(role.getId()).stream()
                .map(RolePermission::getPermissionId)
                .map(permissionsById::get)
                .filter(permission -> permission != null)
                .sorted(Comparator.comparing(Permission::getCode))
                .map(PermissionResponse::from)
                .toList();

        return new RolePermissionSummaryResponse(
                role.getId(),
                role.getName(),
                role.getCode(),
                role.getDescription(),
                permissions
        );
    }
}
