package com.enterprise.auth.seeder;

import com.enterprise.auth.entity.Role;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            log.info("Seeding default roles...");
            Role adminRole = new Role();
            adminRole.setCode("SUPER_ADMIN");
            adminRole.setName("系統管理員");
            adminRole.setDescription("擁有系統內所有資源的支配權限");
            roleRepository.save(adminRole);
        }

        if (userRepository.count() == 0) {
            log.info("Seeding default admin user...");
            User admin = new User();
            admin.setUsername("admin");
            // 使用嚴格的 BCrypt 雜湊保護預設密碼
            admin.setPasswordHash(passwordEncoder.encode("123456"));
            admin.setEmail("admin@enterprise.local");
            admin.setStatus("ACTIVE");
            admin.setFailedAttempts(0);
            userRepository.save(admin);
            log.info(">> Default admin user created successfully! Account: admin / Password: 123456 <<");
        }
    }
}
