package com.example.kursach.controller;

import com.example.kursach.DTO.AnswerDto;
import com.example.kursach.DTO.AnswerUpdateRequest; // <-- ИМПОРТ
import com.example.kursach.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @GetMapping("/questions/{questionId}/answers")
    public ResponseEntity<List<AnswerDto>> getAnswersForQuestion(@PathVariable Long questionId) {
        List<AnswerDto> answers = answerService.getAnswersForQuestion(questionId);
        return ResponseEntity.ok(answers);
    }

    @PostMapping("/questions/{questionId}/answers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerDto> addAnswer(@PathVariable Long questionId,
                                               @Valid @RequestBody AnswerDto answerDto) { // При создании пока принимаем AnswerDto
        AnswerDto createdAnswer = answerService.addAnswer(questionId, answerDto);
        return new ResponseEntity<>(createdAnswer, HttpStatus.CREATED);
    }

    // --- ИЗМЕНЕНО ---
    @PutMapping("/answers/{answerId}")
    @PreAuthorize("isAuthenticated()") // Права проверяются в сервисе
    public ResponseEntity<AnswerDto> updateAnswer(
            @PathVariable Long answerId,
            @Valid @RequestBody AnswerUpdateRequest updateRequest) { // <-- ИЗМЕНЕН ТИП DTO
        AnswerDto updatedAnswer = answerService.updateAnswer(answerId, updateRequest);
        return ResponseEntity.ok(updatedAnswer);
    }
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---

    @DeleteMapping("/answers/{answerId}")
    @PreAuthorize("isAuthenticated()") // Права проверяются в сервисе
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long answerId) {
        answerService.deleteAnswer(answerId);
        return ResponseEntity.noContent().build();
    }
}