package com.example.kursach.exception; // Убедитесь, что пакет правильный

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение для обозначения некорректных запросов (HTTP 400 Bad Request).
 * Например, попытка проголосовать за свой пост, неверные данные и т.д.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST) // Автоматически устанавливает HTTP статус ответа
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}