package com.enterprise.auth.seeder;

import com.enterprise.auth.entity.Role;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.entity.UserRole;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.repository.UserRoleRepository;
import com.enterprise.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByCode("SUPER_ADMIN").orElseGet(() -> {
            log.info("Seeding default roles...");
            Role newAdminRole = new Role();
            newAdminRole.setCode("SUPER_ADMIN");
            newAdminRole.setName("系統管理員");
            newAdminRole.setDescription("擁有系統內所有資源的支配權限");
            return roleRepository.save(newAdminRole);
        });

        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            log.info("Seeding default admin user...");
            User newAdmin = new User();
            newAdmin.setUsername("admin");
            // 使用嚴格的 BCrypt 雜湊保護預設密碼
            newAdmin.setPasswordHash(passwordEncoder.encode("123456"));
            newAdmin.setEmail("admin@enterprise.local");
            newAdmin.setStatus("ACTIVE");
            newAdmin.setFailedAttempts(0);
            log.info(">> Default admin user created successfully! Account: admin / Password: 123456 <<");
            return userRepository.save(newAdmin);
        });

        if (!userRoleRepository.existsByUserIdAndRoleId(admin.getId(), adminRole.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(adminRole.getId());
            userRoleRepository.save(userRole);
        }
    }
}
