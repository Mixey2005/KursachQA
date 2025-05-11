package com.example.kursach.exception; // Убедитесь, что пакет правильный

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение для обозначения отказа в доступе (HTTP 403 Forbidden).
 * Используется, когда пользователь аутентифицирован, но не имеет прав на выполнение операции.
 *
 * Примечание: Существует также org.springframework.security.access.AccessDeniedException.
 * Мы создаем свой для единообразия или специфичной логики, если она потребуется.
 * Если специфичной логики нет, можно использовать спринговый и адаптировать GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // Автоматически устанавливает HTTP статус ответа
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}