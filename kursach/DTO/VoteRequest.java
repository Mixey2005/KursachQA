package com.example.kursach.DTO; // Убедитесь, что пакет правильный

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {
    @NotNull(message = "Значение голоса не может быть пустым")
    private Integer value; // Поле называется value, метод будет getValue()
}