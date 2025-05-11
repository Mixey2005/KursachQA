package com.example.kursach.exception; // Убедитесь, что пакет правильный

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L; // Хорошая практика

    // Оставьте или добавьте конструктор без аргументов, если он нужен где-то еще
    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    // ---> ДОБАВЬТЕ ЭТОТ КОНСТРУКТОР <---
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s не найден(а) с %s : '%s'", resourceName, fieldName, fieldValue));
        // Можно сохранить поля resourceName, fieldName, fieldValue, если они нужны для дальнейшей обработки
    }

    // Можно добавить другие конструкторы при необходимости
}