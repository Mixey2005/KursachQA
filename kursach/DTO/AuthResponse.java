package com.example.kursach.DTO; // Убедитесь, что путь к DTO верный

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer"; // Стандартный тип токена

    // Конструктор только для токена, тип по умолчанию
    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}