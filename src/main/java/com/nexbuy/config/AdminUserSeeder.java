package com.nexbuy.config;

import com.nexbuy.entity.Role;
import com.nexbuy.entity.User;
import com.nexbuy.patterns.builder.UserBuilder;
import com.nexbuy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Override
    public void run(String... args) {
        String email = adminEmail.toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = new UserBuilder()
                .name(adminName)
                .email(email)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Seeded admin user: {}", email);
    }
}
