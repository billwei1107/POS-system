package com.enterprise.auth.service.impl;

import com.enterprise.auth.dto.CreateUserRequest;
import com.enterprise.auth.dto.RegisterRequest;
import com.enterprise.auth.dto.ResetUserPasswordRequest;
import com.enterprise.auth.dto.RoleResponse;
import com.enterprise.auth.dto.UpdateUserRolesRequest;
import com.enterprise.auth.dto.UpdateUserStatusRequest;
import com.enterprise.auth.dto.UserResponse;
import com.enterprise.auth.dto.UserRoleSummaryResponse;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.entity.UserRole;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.repository.UserRepository;
import com.enterprise.auth.repository.UserRoleRepository;
import com.enterprise.auth.service.UserService;
import com.enterprise.common.dto.PageResponse;
import com.enterprise.common.exception.BusinessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserRoleRepository userRoleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(409, "Username already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus("ACTIVE");
        user.setFailedAttempts(0);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserRoleSummaryResponse createManagedUser(CreateUserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new BusinessException(409, "Username already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setStatus("ACTIVE");
        user.setFailedAttempts(0);
        User savedUser = userRepository.save(user);

        replaceUserRoles(savedUser, request.roleIds() == null ? List.of() : request.roleIds());
        return toUserRoleSummary(savedUser);
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleSummaryResponse> getUserRoleSummaries() {
        Map<UUID, Role> rolesById = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getId, role -> role));
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getUsername))
                .map(user -> toUserRoleSummary(user, rolesById))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserRoleSummaryResponse> searchUserRoleSummaries(
            String keyword,
            String status,
            UUID roleId,
            Pageable pageable
    ) {
        Pageable safePageable = PageRequest.of(
                Math.max(pageable.getPageNumber(), 0),
                Math.min(Math.max(pageable.getPageSize(), 1), 100),
                pageable.getSort()
        );
        Map<UUID, Role> rolesById = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getId, role -> role));
        return PageResponse.of(userRepository
                .findAll(buildUserFilter(keyword, status, roleId), safePageable)
                .map(user -> toUserRoleSummary(user, rolesById)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserRoleSummaryResponse getUserRoleSummary(UUID id) {
        return toUserRoleSummary(getUserById(id));
    }

    @Override
    @Transactional
    public UserRoleSummaryResponse updateUserRoles(UUID userId, UpdateUserRolesRequest request) {
        User user = getUserById(userId);
        List<UUID> roleIds = request == null || request.roleIds() == null
                ? List.of()
                : request.roleIds();
        replaceUserRoles(user, roleIds);

        return toUserRoleSummary(user);
    }

    @Override
    @Transactional
    public UserRoleSummaryResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request) {
        User user = getUserById(userId);
        String status = request == null ? "" : request.status();
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new BusinessException(400, "Unsupported user status: " + status);
        }

        if (!"ACTIVE".equals(status)) {
            preventDisablingLastSuperAdmin(user);
        }

        user.setStatus(status);
        if ("ACTIVE".equals(status)) {
            user.setLockedUntil(null);
            user.setFailedAttempts(0);
        }
        userRepository.save(user);
        return toUserRoleSummary(user);
    }

    @Override
    @Transactional
    public UserRoleSummaryResponse resetPassword(UUID userId, ResetUserPasswordRequest request) {
        User user = getUserById(userId);
        if (request == null || request.password() == null || request.password().isBlank()) {
            throw new BusinessException(400, "Password cannot be blank");
        }

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        return toUserRoleSummary(user);
    }

    @Override
    public void handleLoginSuccess(User user) {
        if (user.getFailedAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    @Override
    public void handleLoginFailure(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        }
        userRepository.save(user);
    }

    private void replaceUserRoles(User user, List<UUID> roleIds) {
        Set<UUID> uniqueRoleIds = new LinkedHashSet<>(roleIds);

        List<Role> roles = roleRepository.findAllById(uniqueRoleIds);
        if (roles.size() != uniqueRoleIds.size()) {
            throw new BusinessException(400, "One or more roles do not exist");
        }

        preventRemovingLastSuperAdmin(user, roles);

        userRoleRepository.deleteAllByUserId(user.getId());
        roles.stream()
                .sorted(Comparator.comparing(Role::getCode))
                .forEach(role -> {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(user.getId());
                    userRole.setRoleId(role.getId());
                    userRoleRepository.save(userRole);
                });
    }

    private void preventRemovingLastSuperAdmin(User user, List<Role> nextRoles) {
        Role superAdminRole = roleRepository.findByCode(SUPER_ADMIN).orElse(null);
        if (superAdminRole == null) {
            return;
        }

        boolean currentlySuperAdmin = userRoleRepository.existsByUserIdAndRoleId(user.getId(), superAdminRole.getId());
        boolean willRemainSuperAdmin = nextRoles.stream()
                .anyMatch(role -> SUPER_ADMIN.equals(role.getCode()));
        if (currentlySuperAdmin && !willRemainSuperAdmin && !hasOtherActiveSuperAdmin(user.getId(), superAdminRole.getId())) {
            throw new BusinessException(400, "At least one SUPER_ADMIN user must remain");
        }
    }

    private void preventDisablingLastSuperAdmin(User user) {
        Role superAdminRole = roleRepository.findByCode(SUPER_ADMIN).orElse(null);
        if (superAdminRole == null) {
            return;
        }

        boolean currentlySuperAdmin = userRoleRepository.existsByUserIdAndRoleId(user.getId(), superAdminRole.getId());
        if (currentlySuperAdmin && !hasOtherActiveSuperAdmin(user.getId(), superAdminRole.getId())) {
            throw new BusinessException(400, "At least one active SUPER_ADMIN user must remain");
        }
    }

    private boolean hasOtherActiveSuperAdmin(UUID currentUserId, UUID superAdminRoleId) {
        return userRoleRepository.findAllByRoleId(superAdminRoleId).stream()
                .map(UserRole::getUserId)
                .filter(userId -> !userId.equals(currentUserId))
                .map(userRepository::findById)
                .flatMap(Optional::stream)
                .anyMatch(user -> "ACTIVE".equals(user.getStatus()));
    }

    private Specification<User> buildUserFilter(String keyword, String status, UUID roleId) {
        return Specification.where(keywordFilter(keyword))
                .and(statusFilter(status))
                .and(roleFilter(roleId));
    }

    private Specification<User> keywordFilter(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("username")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("phone")), pattern)
        );
    }

    private Specification<User> statusFilter(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalizedStatus = status.trim().toUpperCase();
        if ("LOCKED".equals(normalizedStatus)) {
            return (root, query, cb) -> cb.greaterThan(root.get("lockedUntil"), LocalDateTime.now());
        }
        if ("ACTIVE".equals(normalizedStatus)) {
            return (root, query, cb) -> cb.and(
                    cb.equal(root.get("status"), normalizedStatus),
                    cb.or(
                            cb.isNull(root.get("lockedUntil")),
                            cb.lessThanOrEqualTo(root.get("lockedUntil"), LocalDateTime.now())
                    )
            );
        }
        return (root, query, cb) -> cb.equal(root.get("status"), normalizedStatus);
    }

    private Specification<User> roleFilter(UUID roleId) {
        if (roleId == null) {
            return null;
        }
        return (root, query, cb) -> {
            var subquery = query.subquery(UUID.class);
            var userRole = subquery.from(UserRole.class);
            subquery.select(userRole.get("userId"))
                    .where(
                            cb.equal(userRole.get("roleId"), roleId),
                            cb.equal(userRole.get("userId"), root.get("id"))
                    );
            return cb.exists(subquery);
        };
    }

    private UserRoleSummaryResponse toUserRoleSummary(User user) {
        Map<UUID, Role> rolesById = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getId, role -> role));
        return toUserRoleSummary(user, rolesById);
    }

    private UserRoleSummaryResponse toUserRoleSummary(User user, Map<UUID, Role> rolesById) {
        List<RoleResponse> roles = userRoleRepository.findAllByUserId(user.getId()).stream()
                .map(UserRole::getRoleId)
                .map(rolesById::get)
                .filter(role -> role != null)
                .sorted(Comparator.comparing(Role::getCode))
                .map(RoleResponse::from)
                .toList();

        return new UserRoleSummaryResponse(UserResponse.from(user), roles);
    }
}
