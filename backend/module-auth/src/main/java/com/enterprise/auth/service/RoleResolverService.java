package com.enterprise.auth.service;

import com.enterprise.auth.entity.Role;
import com.enterprise.auth.entity.UserRole;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @file RoleResolverService.java
 * @description 使用者主要角色解析服務 / Primary user role resolver
 * @description_en Resolves the highest-priority role code for JWT claims and POS sessions
 * @description_zh 解析使用者最高優先級角色代碼，用於 JWT 與 POS 登入回傳
 */
@Service
@RequiredArgsConstructor
public class RoleResolverService {

    private static final String DEFAULT_ROLE = "USER";
    private static final Map<String, Integer> ROLE_PRIORITY = Map.of(
            "SUPER_ADMIN", 100,
            "AREA_MANAGER", 80,
            "STORE_MANAGER", 70,
            "SHIFT_MANAGER", 60,
            "CASHIER", 50,
            "USER", 10
    );

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public String resolvePrimaryRoleCode(UUID userId) {
        return findRoleCodes(userId).stream()
                .max(Comparator.comparingInt(this::priorityOf))
                .orElse(DEFAULT_ROLE);
    }

    public List<String> findRoleCodes(UUID userId) {
        return userRoleRepository.findAllByUserId(userId).stream()
                .map(UserRole::getRoleId)
                .map(roleRepository::findById)
                .flatMap(Optional::stream)
                .map(Role::getCode)
                .toList();
    }

    private int priorityOf(String roleCode) {
        return ROLE_PRIORITY.getOrDefault(roleCode, 0);
    }
}
