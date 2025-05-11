package com.example.kursach.DTO;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private boolean enabled; // Добавлено
    private boolean locked;  // Добавлено
    // private LocalDateTime createdAt;
}