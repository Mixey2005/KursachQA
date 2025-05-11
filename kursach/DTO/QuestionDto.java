package com.example.kursach.DTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class QuestionDto {
    private Long id;
    private String title;
    private String body;
    private String authorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int voteCount;
    private Set<String> tagNames = new HashSet<>();
    private Long acceptedAnswerId;
    private List<AnswerDto> answers = new ArrayList<>();
    private List<CommentDto> comments = new ArrayList<>();
    private Integer currentUserVote;
    private boolean canEdit; // <-- ДОБАВЛЕНО: Может ли текущий юзер редактировать
    private boolean canDelete; // <-- ДОБАВЛЕНО: Может ли текущий юзер удалять
}