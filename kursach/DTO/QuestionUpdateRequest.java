package com.example.kursach.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class QuestionUpdateRequest {
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 5, max = 255, message = "Длина заголовка должна быть от 5 до 255 символов")
    private String title;

    @NotBlank(message = "Текст вопроса не может быть пустым")
    private String body;

    // Теги можно не делать обязательными при редактировании
    private Set<String> tagNames;
}