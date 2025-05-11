package com.example.kursach.service; // Убедитесь, что пакет правильный

// Используйте правильные пакеты для ваших DTO
import com.example.kursach.DTO.JwtResponse;
import com.example.kursach.DTO.LoginRequest;
import com.example.kursach.DTO.RegisterRequest;
import com.example.kursach.DTO.UserDto;

/**
 * Сервис для аутентификации и регистрации пользователей.
 */
public interface AuthService {

    /**
     * Аутентифицирует пользователя и возвращает JWT токен.
     * @param loginRequest Данные для входа (имя пользователя, пароль).
     * @return Ответ с JWT токеном и информацией о пользователе.
     */
    JwtResponse authenticateUser(LoginRequest loginRequest); // Имя метода совпадает с реализацией

    /**
     * Регистрирует нового пользователя.
     * @param registerRequest Данные для регистрации.
     * @return DTO созданного пользователя.
     */
    UserDto registerUser(RegisterRequest registerRequest); // Имя метода совпадает с реализацией

}