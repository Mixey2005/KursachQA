package com.example.kursach.controller; // Убедитесь, что пакет правильный

// Используйте правильные пакеты для ваших DTO
import com.example.kursach.DTO.JwtResponse;
import com.example.kursach.DTO.LoginRequest;
import com.example.kursach.DTO.RegisterRequest;
import com.example.kursach.DTO.UserDto;
import com.example.kursach.service.AuthService; // Используйте ваш пакет service
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth") // Префикс для аутентификации/регистрации
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // Внедряем сервис аутентификации

    /**
     * Эндпоинт для регистрации нового пользователя.
     * @param registerRequest Данные для регистрации.
     * @return ResponseEntity с DTO созданного пользователя.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Вызываем правильный метод сервиса
        UserDto registeredUser = authService.registerUser(registerRequest); // ИЗМЕНЕНО: register -> registerUser
        // Возвращаем 200 OK и данные пользователя (можно вернуть 201 Created)
        return ResponseEntity.ok(registeredUser);
    }

    /**
     * Эндпоинт для аутентификации пользователя.
     * @param loginRequest Данные для входа (логин/пароль).
     * @return ResponseEntity с JWT токеном и основной информацией о пользователе.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Вызываем правильный метод сервиса
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest); // ИЗМЕНЕНО: login -> authenticateUser
        return ResponseEntity.ok(jwtResponse);
    }
}