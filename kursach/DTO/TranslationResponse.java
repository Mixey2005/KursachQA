package com.example.kursach.DTO; // Убедитесь, что пакет правильный

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // <--- Этот конструктор теперь есть
public class TranslationResponse {
    private String translatedText;
    private String detectedSourceLanguage;
}