package com.example.kursach.controller;

import com.example.kursach.DTO.UserDto;
import com.example.kursach.service.AnswerService; // <-- Импорт
import com.example.kursach.service.CommentService;
import com.example.kursach.service.QuestionService;
import com.example.kursach.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Требуется роль ADMIN для всего контроллера
public class AdminController {

    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService; // <-- Добавлено
    private final CommentService commentService;

    // --- Управление пользователями ---
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        Page<UserDto> usersPage = userService.getAllUsers(pageable);
        return ResponseEntity.ok(usersPage);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // --- ДОБАВЛЕНО: Блокировка/Разблокировка ---
    @PatchMapping("/users/{userId}/block") // Используем PATCH для частичного обновления статуса
    public ResponseEntity<Void> blockUser(@PathVariable Long userId) {
        userService.blockUser(userId);
        return ResponseEntity.ok().build(); // Возвращаем 200 OK
    }

    @PatchMapping("/users/{userId}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        userService.unblockUser(userId);
        return ResponseEntity.ok().build();
    }
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---


    // --- Управление контентом ---
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestionByAdmin(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswerByAdmin(@PathVariable Long answerId) {
        answerService.deleteAnswer(answerId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentByAdmin(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}