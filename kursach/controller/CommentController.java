package com.example.kursach.controller; // Убедитесь, что пакет правильный

import com.example.kursach.DTO.CommentDto; // Используйте ваш пакет DTO
import com.example.kursach.service.CommentService; // Используйте ваш пакет service
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1") // Общий префикс
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // --- Получение комментариев ---

    // Получить комментарии к ВОПРОСУ
    @GetMapping("/questions/{questionId}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsForQuestion(@PathVariable Long questionId) {
        // Используем правильное имя метода из CommentService
        List<CommentDto> comments = commentService.getCommentsForQuestion(questionId);
        return ResponseEntity.ok(comments);
    }

    // Получить комментарии к ОТВЕТУ
    @GetMapping("/answers/{answerId}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsForAnswer(@PathVariable Long answerId) {
        // Используем правильное имя метода из CommentService
        List<CommentDto> comments = commentService.getCommentsForAnswer(answerId);
        return ResponseEntity.ok(comments);
    }

    // --- Создание комментариев ---

    // Добавить комментарий к ВОПРОСУ
    @PostMapping("/questions/{questionId}/comments")
    @PreAuthorize("isAuthenticated()") // Только аутентифицированные пользователи
    public ResponseEntity<CommentDto> addCommentToQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody CommentDto commentDto) { // Принимаем CommentDto
        // Используем метод для добавления к вопросу
        CommentDto createdComment = commentService.addCommentToQuestion(questionId, commentDto);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    // Добавить комментарий к ОТВЕТУ
    @PostMapping("/answers/{answerId}/comments")
    @PreAuthorize("isAuthenticated()") // Только аутентифицированные пользователи
    public ResponseEntity<CommentDto> addCommentToAnswer(
            @PathVariable Long answerId,
            @Valid @RequestBody CommentDto commentDto) { // Принимаем CommentDto
        // Используем метод для добавления к ответу
        CommentDto createdComment = commentService.addCommentToAnswer(answerId, commentDto);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    // --- Удаление комментария ---

    @DeleteMapping("/comments/{commentId}")
    // Права проверяются в сервисе (владелец) ИЛИ нужен @postSecurityService ИЛИ проверка роли ADMIN
    @PreAuthorize("isAuthenticated()") // Базовая проверка, остальное в сервисе или @postSecurityService
    // Либо: @PreAuthorize("isAuthenticated() and (@postSecurityService.isCommentOwner(#commentId) or hasRole('ADMIN'))")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}