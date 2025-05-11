package com.example.kursach.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private String body;
    private String authorUsername;
    private Long questionId;
    private Long answerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int voteCount;
    private Integer currentUserVote;
    private boolean canDelete; // <-- ДОБАВЛЕНО (Редактирование комментов обычно не делают)
}