package com.enterprise.auth.repository;

import com.enterprise.auth.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @file RolePermissionRepository.java
 * @description 角色權限關聯 Repository / Role-permission mapping repository
 * @description_en Queries permission assignments for role-based access control
 * @description_zh 查詢角色權限指派，供 RBAC 權限檢查使用
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermission.RolePermissionId> {
    List<RolePermission> findAllByRoleId(UUID roleId);

    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    void deleteAllByRoleId(UUID roleId);
}
