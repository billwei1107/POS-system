package com.enterprise.auth.seeder;

import com.enterprise.auth.entity.Permission;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.repository.PermissionRepository;
import com.enterprise.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * POS 角色與權限種子資料 / POS roles and permissions seeder
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pos.auth.pin-login", havingValue = "true")
@Order(2)
@Slf4j
public class PosDataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedPermissions();
        log.info("POS seed data initialized successfully");
    }

    private void seedRoles() {
        createRoleIfNotExists("CASHIER", "收銀員", "POS 收銀員 / POS Cashier");
        createRoleIfNotExists("SHIFT_MANAGER", "值班主管", "POS 值班主管 / Shift Manager");
        createRoleIfNotExists("STORE_MANAGER", "店長", "POS 店長 / Store Manager");
        createRoleIfNotExists("AREA_MANAGER", "區域經理", "POS 區域經理 / Area Manager");
    }

    private void seedPermissions() {
        createPermissionIfNotExists("pos:order:create", "建立訂單", "POS", "order", "create");
        createPermissionIfNotExists("pos:order:void", "作廢訂單", "POS", "order", "void");
        createPermissionIfNotExists("pos:order:refund", "退款", "POS", "order", "refund");
        createPermissionIfNotExists("pos:discount:apply", "套用折扣", "POS", "discount", "apply");
        createPermissionIfNotExists("pos:report:view", "查看報表", "POS", "report", "view");
        createPermissionIfNotExists("pos:report:view-all", "查看全店報表", "POS", "report", "view-all");
        createPermissionIfNotExists("pos:settings:manage", "管理設定", "POS", "settings", "manage");
        createPermissionIfNotExists("pos:employee:manage", "管理員工", "POS", "employee", "manage");
        createPermissionIfNotExists("pos:inventory:adjust", "庫存調整", "POS", "inventory", "adjust");
        createPermissionIfNotExists("pos:price:override", "價格覆蓋", "POS", "price", "override");
    }

    private void createRoleIfNotExists(String code, String name, String description) {
        if (roleRepository.findByCode(code).isEmpty()) {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
        }
    }

    private void createPermissionIfNotExists(String code, String name, String type, String resource, String action) {
        if (permissionRepository.findByCode(code).isEmpty()) {
            Permission permission = new Permission();
            permission.setCode(code);
            permission.setName(name);
            permission.setType(type);
            permission.setResource(resource);
            permission.setAction(action);
            permissionRepository.save(permission);
        }
    }
}
