package com.example.kursach.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnswerUpdateRequest {
    @NotBlank(message = "Текст ответа не может быть пустым")
    private String body;
}