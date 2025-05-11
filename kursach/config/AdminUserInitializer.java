package com.example.kursach.config;

import com.example.kursach.entity.Role;
import com.example.kursach.entity.User;
import com.example.kursach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;
    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername(adminUsername)) {
            log.info("Администратор по умолчанию не найден. Создание нового администратора...");

            User adminUser = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .enabled(true) // <-- Устанавливаем enabled
                    .build();

            userRepository.save(adminUser);
            log.info("Администратор по умолчанию '{}' успешно создан.", adminUsername);
        } else {
            log.info("Администратор по умолчанию '{}' уже существует.", adminUsername);
        }
    }
}