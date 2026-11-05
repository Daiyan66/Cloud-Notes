package com.cloudnotes.config;

import com.cloudnotes.user.Role;
import com.cloudnotes.user.User;
import com.cloudnotes.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@cloudnotes.edu}")
    private String adminEmail;

    @Value("${app.seed.admin-password:Admin@CloudNotes2026}")
    private String adminPassword;

    @Value("${app.seed.admin-name:System Administrator}")
    private String adminName;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail.toLowerCase().trim())) {
            User admin = User.builder()
                    .name(adminName)
                    .email(adminEmail.toLowerCase().trim())
                    .password(passwordEncoder.encode(adminPassword))
                    .branch("Administration")
                    .semester(null)
                    .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_STUDENT))
                    .enabled(true)
                    .emailVerified(true)
                    .build();

            userRepository.save(admin);
            log.info("Default administrator seeded: {}", adminEmail);
        } else {
            log.info("Administrator account already exists.");
        }
    }
}
