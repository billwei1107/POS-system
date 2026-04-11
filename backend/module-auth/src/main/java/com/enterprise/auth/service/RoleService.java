package com.enterprise.auth.service;

import com.enterprise.auth.entity.Role;
import java.util.List;
import java.util.UUID;

public interface RoleService {
    Role getRoleById(UUID id);
    List<Role> getAllRoles();
}
