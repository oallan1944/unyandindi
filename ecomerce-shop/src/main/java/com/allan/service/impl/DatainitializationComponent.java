package com.allan.service.impl;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.allan.domain.USER_ROLE;
import com.allan.model.User;
import com.allan.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatainitializationComponent implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        String adminUsername = "yamaoallan1@gmail.com";
        if (userRepository.findByEmail(adminUsername) == null) {
            User adminUser = new User();

            adminUser.setPassword(passwordEncoder.encode("Unyandindi_02"));
            adminUser.setFullName("Allan Ouma");
            adminUser.setEmail(adminUsername);
            adminUser.setRole(USER_ROLE.ROLE_ADMIN);

             userRepository.save(adminUser);
        }
    }
}
