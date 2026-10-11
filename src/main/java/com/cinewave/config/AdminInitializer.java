package com.cinewave.config;

import com.cinewave.entity.Role;
import com.cinewave.entity.User;
import com.cinewave.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * Initializes the first admin user on startup if ADMIN_EMAIL and ADMIN_PASSWORD
 * environment variables are set and no admin exists yet.
 * 
 * This is the ONLY way to create an admin — public registration always creates USER accounts.
 */
@Configuration
public class AdminInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    @Value("${cinewave.admin.email:}")
    private String adminEmail;

    @Value("${cinewave.admin.password:}")
    private String adminPassword;

    @Value("${cinewave.admin.name:Admin}")
    private String adminName;

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
                log.info("No ADMIN_EMAIL/ADMIN_PASSWORD set. Skipping admin initialization.");
                return;
            }

            if (userRepository.existsByEmail(adminEmail)) {
                log.info("Admin account already exists for: {}", adminEmail);
                return;
            }

            User admin = new User(adminName, adminEmail, encoder.encode(adminPassword), Role.ADMIN);
            userRepository.save(admin);
            log.info("✅ Initial admin created: {}", adminEmail);
        };
    }
}
