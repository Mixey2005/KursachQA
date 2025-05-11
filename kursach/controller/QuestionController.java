package com.example.kursach.controller;

import com.example.kursach.DTO.AnswerDto;
import com.example.kursach.DTO.QuestionDto;
import com.example.kursach.DTO.QuestionUpdateRequest; // <-- ИМПОРТ
import com.example.kursach.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<Page<QuestionDto>> getAllQuestions(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<QuestionDto> questions = questionService.getAllQuestions(pageable);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDto> getQuestionById(@PathVariable Long id) {
        QuestionDto question = questionService.getQuestionById(id);
        return ResponseEntity.ok(question);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuestionDto> createQuestion(@Valid @RequestBody QuestionDto questionDto) {
        QuestionDto createdQuestion = questionService.createQuestion(questionDto);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }

    // --- ИЗМЕНЕНО ---
    @PutMapping("/{questionId}")
    @PreAuthorize("isAuthenticated()") // Права проверяются в сервисе
    public ResponseEntity<QuestionDto> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionUpdateRequest updateRequest) { // <-- ИЗМЕНЕН ТИП DTO
        QuestionDto updatedQuestion = questionService.updateQuestion(questionId, updateRequest);
        return ResponseEntity.ok(updatedQuestion);
    }
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---

    @DeleteMapping("/{questionId}")
    @PreAuthorize("isAuthenticated()") // Права проверяются в сервисе
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{questionId}/accept/{answerId}")
    @PreAuthorize("isAuthenticated()") // Права проверяются в сервисе
    public ResponseEntity<AnswerDto> markAnswerAsAccepted(@PathVariable Long questionId,
                                                          @PathVariable Long answerId) {
        AnswerDto acceptedAnswer = questionService.markAnswerAsAccepted(questionId, answerId);
        return ResponseEntity.ok(acceptedAnswer);
    }
}