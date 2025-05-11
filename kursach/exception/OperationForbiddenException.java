package com.example.kursach.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое, когда запрашиваемая операция запрещена
 * (например, пользователь пытается удалить себя, админ пытается изменить свою роль).
 * Возвращает HTTP статус 403 Forbidden.
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // Устанавливаем HTTP статус ответа
public class OperationForbiddenException extends RuntimeException {
    public OperationForbiddenException(String message) {
        super(message);
    }
}