package com.example.kursach.DTO;

import com.example.kursach.entity.Role; // Импорт Role
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: генерирует геттеры, сеттеры, toString, equals, hashCode
@NoArgsConstructor // Lombok: генерирует конструктор без аргументов (нужен для Jackson/JPA)
public class AdminUserUpdateRequest {

    @NotNull(message = "Роль не может быть пустой")
    private Role role; // Поле для изменения роли

    @NotNull(message = "Статус активности не может быть пустым")
    private Boolean enabled; // Поле для блокировки/разблокировки (true=активен, false=заблокирован)
}