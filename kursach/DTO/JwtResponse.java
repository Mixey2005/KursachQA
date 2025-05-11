package com.example.kursach.DTO; // Убедитесь, что пакет правильный

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer"; // Стандартный тип токена
    private Long id;
    private String username;
    private String email;
    private String role; // Используем строку для роли

    // Конструктор для удобного создания объекта
    public JwtResponse(String accessToken, Long id, String username, String email, String role) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}