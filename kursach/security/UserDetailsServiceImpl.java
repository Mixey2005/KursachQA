package com.example.kursach.security; // Ваш пакет

import com.example.kursach.entity.User;
import com.example.kursach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Для логов
import org.slf4j.LoggerFactory; // Для логов
import org.springframework.security.core.userdetails.UserDetails; // <-- ИМЕННО ЭТОТ ИМПОРТ
import org.springframework.security.core.userdetails.UserDetailsService; // <-- ИМЕННО ЭТОТ ИМПОРТ
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService { // <-- РЕАЛИЗУЕТ ЭТОТ ИНТЕРФЕЙС

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class); // Логгер
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    // --- ВАЖНО: СИГНАТУРА МЕТОДА ---
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // <-- ВОЗВРАЩАЕТ UserDetails
        // --- КОНЕЦ ВАЖНОГО УЧАСТКА ---
        log.debug("Загрузка пользователя по имени: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден с именем пользователя: {}", username);
                    return new UsernameNotFoundException("Пользователь не найден с именем пользователя: " + username);
                });
        log.debug("Пользователь {} найден, создание UserDetailsImpl", username);
        // --- ВАЖНО: ВОЗВРАЩАЕМОЕ ЗНАЧЕНИЕ ---
        return UserDetailsImpl.build(user); // <-- ВОЗВРАЩАЕТ UserDetailsImpl (который реализует UserDetails)
        // --- КОНЕЦ ВАЖНОГО УЧАСТКА ---
    }
}