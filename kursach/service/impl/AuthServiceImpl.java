package com.example.kursach.service.impl; // Убедитесь, что пакет правильный

import com.example.kursach.DTO.LoginRequest;
import com.example.kursach.DTO.RegisterRequest;
import com.example.kursach.DTO.JwtResponse;
import com.example.kursach.DTO.UserDto;
import com.example.kursach.entity.Role;
import com.example.kursach.entity.User;
import com.example.kursach.exception.BadRequestException;
import com.example.kursach.repository.UserRepository;
import com.example.kursach.security.JwtUtil;
import com.example.kursach.security.UserDetailsImpl; // Убедитесь, что этот класс существует и импортирован
import com.example.kursach.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger; // Логгер для отладки
import org.slf4j.LoggerFactory; // Логгер для отладки


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class); // Добавим логгер

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService; // <-- Убедитесь, что тип здесь UserDetailsService

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        log.debug("Попытка аутентификации пользователя: {}", loginRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        log.debug("Аутентификация успешна для: {}", loginRequest.getUsername());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails;
        Object principal = authentication.getPrincipal();
        log.debug("Тип Principal после аутентификации: {}", (principal != null ? principal.getClass().getName() : "null"));

        if (principal instanceof UserDetails) {
            userDetails = (UserDetails) principal;
            log.debug("Principal является UserDetails.");
        }
        // --- ПОПЫТКА ИСПРАВЛЕНИЯ ОШИБКИ ТИПОВ ---
        // Если предыдущий вариант не сработал, возможно, проблема в том, как Spring возвращает Principal.
        // Попробуем получить имя пользователя и загрузить UserDetails явно.
        else {
            log.warn("Principal НЕ является UserDetails. Попытка загрузить по имени пользователя: {}", authentication.getName());
            String username = authentication.getName();
            if (username == null) {
                throw new IllegalStateException("Имя пользователя null в объекте Authentication после успешной аутентификации.");
            }
            if(userDetailsService == null) {
                throw new IllegalStateException("UserDetailsService не был внедрен (null)");
            }

            // --- ДИАГНОСТИЧЕСКАЯ СТРОКА ---
            log.info("Класс внедренного UserDetailsService: {}", userDetailsService.getClass().getName());
            // --- КОНЕЦ ДИАГНОСТИЧЕСКОЙ СТРОКИ ---

            try {
                // Проблемная строка:
                userDetails = userDetailsService.loadUserByUsername(username);
                log.debug("UserDetails успешно загружен для: {}", username);
            } catch (Exception e) {
                log.error("Ошибка при загрузке UserDetails для {}: {}", username, e.getMessage(), e);
                throw new IllegalStateException("Не удалось загрузить UserDetails после аутентификации для пользователя: " + username, e);
            }
        }

        if (userDetails == null) { // Дополнительная проверка
            throw new IllegalStateException("Объект UserDetails равен null после попытки получения/загрузки.");
        }

        log.debug("Генерация JWT для: {}", userDetails.getUsername());
        String jwt = jwtUtil.generateToken(userDetails.getUsername());

        // Получение доп. информации остается как было
        Long userId = null;
        String email = null;
        String role = null;
        // ... (код получения userId, email, role как в предыдущем ответе) ...
        if (userDetails instanceof UserDetailsImpl) {
            UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
            userId = userDetailsImpl.getId();
            email = userDetailsImpl.getEmail();
            if (userDetailsImpl.getAuthorities() != null && !userDetailsImpl.getAuthorities().isEmpty()) {
                role = userDetailsImpl.getAuthorities().iterator().next().getAuthority();
            }
        } else if (userDetails instanceof User && userDetails instanceof UserDetails) { // Проверка для User, если он реализует UserDetails
            User user = (User) userDetails;
            userId = user.getId();
            email = user.getEmail();
            if (user.getRole() != null) {
                role = "ROLE_" + user.getRole().name();
            }
        } else {
            User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (foundUser != null) {
                userId = foundUser.getId();
                email = foundUser.getEmail();
                if (foundUser.getRole() != null) {
                    role = "ROLE_" + foundUser.getRole().name();
                }
            } else {
                userId = -1L;
                email = "N/A";
                if (userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
                    role = userDetails.getAuthorities().iterator().next().getAuthority();
                } else {
                    role = "N/A";
                }
            }
        }
        log.debug("Информация для JwtResponse: id={}, username={}, email={}, role={}", userId, userDetails.getUsername(), email, role);

        return new JwtResponse(jwt, userId, userDetails.getUsername(), email, role);
    }

    @Override
    @Transactional
    public UserDto registerUser(RegisterRequest registerRequest) {
        log.debug("Попытка регистрации пользователя: {}", registerRequest.getUsername());
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("Имя пользователя {} уже занято", registerRequest.getUsername());
            throw new BadRequestException("Ошибка: Имя пользователя '" + registerRequest.getUsername() + "' уже занято!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Email {} уже используется", registerRequest.getEmail());
            throw new BadRequestException("Ошибка: Email '" + registerRequest.getEmail() + "' уже используется!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER); // Роль по умолчанию

        User savedUser = userRepository.save(user);
        log.info("Пользователь {} успешно зарегистрирован с ID {}", savedUser.getUsername(), savedUser.getId());
        return mapUserToDto(savedUser);
    }

    private UserDto mapUserToDto(User user) {
        if (user == null) return null;
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole() != null ? user.getRole().name() : null);
        return userDto;
    }
}