package com.example.kursach.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentCreateRequest {

    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 2000, message = "Комментарий не может превышать 2000 символов")
    private String body;
}