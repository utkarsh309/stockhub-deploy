// Create new file:
// config/DataInitializer.java

package com.stockhub.auth.config;

import com.stockhub.auth.entity.User;
import com.stockhub.auth.enums.Role;
import com.stockhub.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        // Check if admin already exists
        if (!userRepository.existsByEmail("utkarshraj309@gmail.com")) {

            User admin = User.builder()
                    .fullName("Utkarsh")
                    .email("utkarshraj309@gmail.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN).
                    department("Management")
                    .isActive(true)
                    .build();

            userRepository.save(admin);
            log.info("Default admin created!");
        }
    }
}