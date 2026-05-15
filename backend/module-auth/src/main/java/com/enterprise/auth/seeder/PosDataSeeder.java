package com.enterprise.auth.seeder;

import com.enterprise.auth.entity.Permission;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.repository.PermissionRepository;
import com.enterprise.auth.repository.RolePermissionRepository;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.entity.RolePermission;
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
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedPermissions();
        seedRolePermissions();
        log.info("POS seed data initialized successfully");
    }

    private void seedRoles() {
        createRoleIfNotExists("CASHIER", "收銀員", "POS 收銀員 / POS Cashier");
        createRoleIfNotExists("SHIFT_MANAGER", "值班主管", "POS 值班主管 / Shift Manager");
        createRoleIfNotExists("STORE_MANAGER", "店長", "POS 店長 / Store Manager");
        createRoleIfNotExists("AREA_MANAGER", "區域經理", "POS 區域經理 / Area Manager");
    }

    private void seedPermissions() {
        createPermissionIfNotExists("pos:order:read", "查看訂單", "POS", "order", "read");
        createPermissionIfNotExists("pos:order:create", "建立訂單", "POS", "order", "create");
        createPermissionIfNotExists("pos:order:void", "作廢訂單", "POS", "order", "void");
        createPermissionIfNotExists("pos:order:refund", "退款", "POS", "order", "refund");
        createPermissionIfNotExists("pos:discount:apply", "套用折扣", "POS", "discount", "apply");
        createPermissionIfNotExists("pos:payment:process", "處理付款", "POS", "payment", "process");
        createPermissionIfNotExists("pos:payment:read", "查看付款資料", "POS", "payment", "read");
        createPermissionIfNotExists("pos:payment:method-manage", "管理付款方式", "POS", "payment", "method-manage");
        createPermissionIfNotExists("pos:cash-drawer:read", "查看錢櫃狀態", "POS", "cash-drawer", "read");
        createPermissionIfNotExists("pos:cash-drawer:manage", "管理錢櫃", "POS", "cash-drawer", "manage");
        createPermissionIfNotExists("pos:reconciliation:read", "查看對帳資料", "POS", "reconciliation", "read");
        createPermissionIfNotExists("pos:reconciliation:manage", "管理對帳", "POS", "reconciliation", "manage");
        createPermissionIfNotExists("pos:shift:read", "查看班次", "POS", "shift", "read");
        createPermissionIfNotExists("pos:shift:operate", "操作班次", "POS", "shift", "operate");
        createPermissionIfNotExists("pos:shift:manage", "管理班次", "POS", "shift", "manage");
        createPermissionIfNotExists("pos:report:view", "查看報表", "POS", "report", "view");
        createPermissionIfNotExists("pos:report:view-all", "查看全店報表", "POS", "report", "view-all");
        createPermissionIfNotExists("pos:settings:manage", "管理設定", "POS", "settings", "manage");
        createPermissionIfNotExists("pos:employee:manage", "管理員工", "POS", "employee", "manage");
        createPermissionIfNotExists("pos:inventory:read", "查看庫存", "POS", "inventory", "read");
        createPermissionIfNotExists("pos:inventory:adjust", "庫存調整", "POS", "inventory", "adjust");
        createPermissionIfNotExists("pos:inventory:receive", "進貨驗收", "POS", "inventory", "receive");
        createPermissionIfNotExists("pos:inventory:stock-take", "盤點作業", "POS", "inventory", "stock-take");
        createPermissionIfNotExists("pos:inventory:transfer", "庫存調撥", "POS", "inventory", "transfer");
        createPermissionIfNotExists("pos:product:read", "查看商品資料", "POS", "product", "read");
        createPermissionIfNotExists("pos:product:manage", "管理商品資料", "POS", "product", "manage");
        createPermissionIfNotExists("pos:member:read", "查看會員資料", "POS", "member", "read");
        createPermissionIfNotExists("pos:member:operate", "操作會員綁定與快速註冊", "POS", "member", "operate");
        createPermissionIfNotExists("pos:member:manage", "管理會員資料", "POS", "member", "manage");
        createPermissionIfNotExists("pos:invoice:read", "查看發票資料", "POS", "invoice", "read");
        createPermissionIfNotExists("pos:invoice:issue", "開立發票", "POS", "invoice", "issue");
        createPermissionIfNotExists("pos:invoice:void", "作廢發票", "POS", "invoice", "void");
        createPermissionIfNotExists("pos:tax:read", "查看稅務設定", "POS", "tax", "read");
        createPermissionIfNotExists("pos:tax:manage", "管理稅務設定", "POS", "tax", "manage");
        createPermissionIfNotExists("pos:invoice-track:manage", "管理發票字軌", "POS", "invoice-track", "manage");
        createPermissionIfNotExists("pos:price:override", "價格覆蓋", "POS", "price", "override");
        createPermissionIfNotExists("system:audit:read", "查看稽核紀錄", "SYSTEM", "audit", "read");
        createPermissionIfNotExists("system:rbac:read", "查看角色權限", "SYSTEM", "rbac", "read");
        createPermissionIfNotExists("system:rbac:manage", "管理角色權限", "SYSTEM", "rbac", "manage");
        createPermissionIfNotExists("system:user:read", "查看使用者帳號", "SYSTEM", "user", "read");
        createPermissionIfNotExists("system:user:manage", "管理使用者角色", "SYSTEM", "user", "manage");
        createPermissionIfNotExists("system:organization:read", "查看組織資料", "SYSTEM", "organization", "read");
        createPermissionIfNotExists("system:organization:manage", "管理組織資料", "SYSTEM", "organization", "manage");
        createPermissionIfNotExists("system:attendance:read", "查看出勤資料", "SYSTEM", "attendance", "read");
        createPermissionIfNotExists("system:attendance:operate", "執行出勤操作", "SYSTEM", "attendance", "operate");
        createPermissionIfNotExists("system:attendance:manage", "管理出勤設定", "SYSTEM", "attendance", "manage");
        createPermissionIfNotExists("system:leave:read", "查看請假資料", "SYSTEM", "leave", "read");
        createPermissionIfNotExists("system:leave:request", "申請與銷假", "SYSTEM", "leave", "request");
        createPermissionIfNotExists("system:leave:manage", "管理假別設定", "SYSTEM", "leave", "manage");
        createPermissionIfNotExists("system:notification:read", "查看通知", "SYSTEM", "notification", "read");
        createPermissionIfNotExists("system:notification:operate", "操作通知已讀狀態", "SYSTEM", "notification", "operate");
        createPermissionIfNotExists("system:notification:manage", "管理通知測試與推播", "SYSTEM", "notification", "manage");
        createPermissionIfNotExists("system:workflow:read", "查看工作流資料", "SYSTEM", "workflow", "read");
        createPermissionIfNotExists("system:workflow:start", "啟動工作流", "SYSTEM", "workflow", "start");
        createPermissionIfNotExists("system:workflow:approve", "審批工作流任務", "SYSTEM", "workflow", "approve");
    }

    private void seedRolePermissions() {
        assignPermissions("CASHIER",
                "pos:order:read",
                "pos:order:create",
                "pos:payment:process",
                "pos:payment:read",
                "pos:cash-drawer:read",
                "pos:shift:read",
                "pos:shift:operate",
                "pos:inventory:read",
                "pos:product:read",
                "pos:member:read",
                "pos:member:operate",
                "pos:invoice:read",
                "pos:invoice:issue",
                "pos:report:view",
                "system:leave:read",
                "system:leave:request",
                "system:notification:read",
                "system:notification:operate");
        assignPermissions("SHIFT_MANAGER",
                "pos:order:read",
                "pos:order:create",
                "pos:order:void",
                "pos:order:refund",
                "pos:discount:apply",
                "pos:payment:process",
                "pos:payment:read",
                "pos:cash-drawer:read",
                "pos:cash-drawer:manage",
                "pos:reconciliation:read",
                "pos:shift:read",
                "pos:shift:operate",
                "pos:shift:manage",
                "pos:report:view",
                "pos:inventory:read",
                "pos:inventory:adjust",
                "pos:inventory:receive",
                "pos:inventory:stock-take",
                "pos:inventory:transfer",
                "pos:product:read",
                "pos:member:read",
                "pos:member:operate",
                "pos:invoice:read",
                "pos:invoice:issue",
                "pos:invoice:void",
                "pos:tax:read",
                "system:leave:read",
                "system:leave:request",
                "system:notification:read",
                "system:notification:operate",
                "system:workflow:read",
                "system:workflow:approve");
        assignPermissions("STORE_MANAGER",
                "pos:order:read",
                "pos:order:create",
                "pos:order:void",
                "pos:order:refund",
                "pos:discount:apply",
                "pos:payment:process",
                "pos:payment:read",
                "pos:payment:method-manage",
                "pos:cash-drawer:read",
                "pos:cash-drawer:manage",
                "pos:reconciliation:read",
                "pos:reconciliation:manage",
                "pos:shift:read",
                "pos:shift:operate",
                "pos:shift:manage",
                "pos:report:view",
                "pos:report:view-all",
                "pos:settings:manage",
                "pos:employee:manage",
                "pos:inventory:read",
                "pos:inventory:adjust",
                "pos:inventory:receive",
                "pos:inventory:stock-take",
                "pos:inventory:transfer",
                "pos:product:read",
                "pos:product:manage",
                "pos:member:read",
                "pos:member:operate",
                "pos:member:manage",
                "pos:invoice:read",
                "pos:invoice:issue",
                "pos:invoice:void",
                "pos:tax:read",
                "pos:tax:manage",
                "pos:invoice-track:manage",
                "pos:price:override",
                "system:leave:read",
                "system:leave:request",
                "system:leave:manage",
                "system:notification:read",
                "system:notification:operate",
                "system:notification:manage",
                "system:workflow:read",
                "system:workflow:start",
                "system:workflow:approve");
        assignPermissions("AREA_MANAGER",
                "pos:order:read",
                "pos:order:create",
                "pos:order:void",
                "pos:order:refund",
                "pos:discount:apply",
                "pos:payment:process",
                "pos:payment:read",
                "pos:payment:method-manage",
                "pos:cash-drawer:read",
                "pos:cash-drawer:manage",
                "pos:reconciliation:read",
                "pos:reconciliation:manage",
                "pos:shift:read",
                "pos:shift:operate",
                "pos:shift:manage",
                "pos:report:view",
                "pos:report:view-all",
                "pos:settings:manage",
                "pos:employee:manage",
                "pos:inventory:read",
                "pos:inventory:adjust",
                "pos:inventory:receive",
                "pos:inventory:stock-take",
                "pos:inventory:transfer",
                "pos:product:read",
                "pos:product:manage",
                "pos:member:read",
                "pos:member:operate",
                "pos:member:manage",
                "pos:invoice:read",
                "pos:invoice:issue",
                "pos:invoice:void",
                "pos:tax:read",
                "pos:tax:manage",
                "pos:invoice-track:manage",
                "pos:price:override",
                "system:audit:read",
                "system:rbac:read",
                "system:user:read",
                "system:organization:read",
                "system:organization:manage",
                "system:attendance:read",
                "system:attendance:operate",
                "system:attendance:manage",
                "system:leave:read",
                "system:leave:request",
                "system:leave:manage",
                "system:notification:read",
                "system:notification:operate",
                "system:notification:manage",
                "system:workflow:read",
                "system:workflow:start",
                "system:workflow:approve");
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

    private void assignPermissions(String roleCode, String... permissionCodes) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Missing role: " + roleCode));
        for (String permissionCode : permissionCodes) {
            Permission permission = permissionRepository.findByCode(permissionCode)
                    .orElseThrow(() -> new IllegalStateException("Missing permission: " + permissionCode));
            if (!rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(role.getId());
                rolePermission.setPermissionId(permission.getId());
                rolePermissionRepository.save(rolePermission);
            }
        }
    }
}
