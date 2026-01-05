package com.wims.backend.configuration;

import com.wims.backend.entity.Role;
import com.wims.backend.entity.User;
import com.wims.backend.repository.RoleRepository;
import com.wims.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // Khoan quan tâm cái này vội
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Tạo Role nếu chưa có
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Full quyền");
            roleRepository.save(adminRole);

            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Khách hàng");
            roleRepository.save(userRole);
        }

        // 2. Tạo User Admin nếu chưa có
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName("ADMIN").get();

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // LƯU Ý: Buổi sau ta sẽ mã hóa cái này
            admin.setEmail("admin@wims.com");
            admin.setFullName("System Admin");

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            Role userRole = roleRepository.findByName("USER").get();

            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123")); // LƯU Ý: Buổi sau ta sẽ mã hóa cái này
            user.setEmail("user@wims.com");
            user.setFullName("Just User");

            Set<Role> uRoles = new HashSet<>();
            uRoles.add(userRole);
            user.setRoles(uRoles);

            userRepository.save(admin);
            userRepository.save(user);
        }
    }
}