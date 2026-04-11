package com.enterprise.auth.service;

import com.enterprise.auth.entity.Permission;
import java.util.List;
import java.util.UUID;

public interface PermissionService {
    Permission getPermissionById(UUID id);
    List<Permission> getAllPermissions();
}
