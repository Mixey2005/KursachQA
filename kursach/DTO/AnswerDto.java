package com.example.kursach.DTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AnswerDto {
    private Long id;
    private String body;
    private Long questionId;
    private String authorUsername;
    private int voteCount;
    private boolean accepted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentDto> comments = new ArrayList<>();
    private Integer currentUserVote;
    private boolean canEdit; // <-- ДОБАВЛЕНО
    private boolean canDelete; // <-- ДОБАВЛЕНО
    private boolean canAccept; // <-- ДОБАВЛЕНО: Может ли текущий юзер принять этот ответ
}