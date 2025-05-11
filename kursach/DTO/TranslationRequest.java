package com.example.kursach.DTO; // Убедитесь, что пакет правильный

import jakarta.validation.constraints.NotBlank; // Добавляем валидацию
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRequest {

    @NotBlank(message = "Текст для перевода не может быть пустым")
    private String text;

    @NotBlank(message = "Целевой язык не может быть пустым")
    @Size(min = 2, max = 5, message = "Код целевого языка должен содержать от 2 до 5 символов") // Пример валидации
    private String targetLanguage;

    @Size(min = 2, max = 5, message = "Код исходного языка должен содержать от 2 до 5 символов")
    private String sourceLanguage; // Может быть null, если хотим автоопределение

    // Lombok @Data сгенерирует getText(), getTargetLanguage(), getSourceLanguage()
}