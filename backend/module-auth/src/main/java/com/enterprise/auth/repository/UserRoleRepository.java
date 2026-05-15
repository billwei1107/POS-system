package com.enterprise.auth.repository;

import com.enterprise.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @file UserRoleRepository.java
 * @description 使用者角色關聯 Repository / User-role mapping repository
 * @description_en Queries role assignments for authorization checks
 * @description_zh 查詢使用者角色指派，供權限判斷使用
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {
    List<UserRole> findAllByUserId(UUID userId);

    List<UserRole> findAllByRoleId(UUID roleId);

    void deleteAllByUserId(UUID userId);

    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);

    long countByRoleId(UUID roleId);
}
