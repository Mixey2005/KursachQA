package com.example.kursach.controller; // Убедитесь, что пакет правильный

import com.example.kursach.DTO.UserDto; // Используйте ваш пакет DTO
import com.example.kursach.service.UserService; // Используйте ваш пакет service
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Для @PreAuthorize
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users") // Общий префикс для эндпоинтов пользователя
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Получает информацию о текущем аутентифицированном пользователе.
     * @return ResponseEntity с UserDto текущего пользователя или ошибку.
     */
    @GetMapping("/me") // Эндпоинт для получения "себя"
    @PreAuthorize("isAuthenticated()") // Доступно только аутентифицированным пользователям
    public ResponseEntity<UserDto> getCurrentUser() {
        // Вызываем новый метод сервиса
        UserDto currentUserDto = userService.getCurrentUserDto();
        return ResponseEntity.ok(currentUserDto);
    }

    // TODO: Добавить другие эндпоинты для пользователя, если нужно
    // Например, получение пользователя по ID (для админа или профиля), обновление профиля и т.д.
    // @GetMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') or #id == principal.id") // Пример проверки прав
    // public ResponseEntity<UserDto> getUserProfile(@PathVariable Long id) { ... }

}