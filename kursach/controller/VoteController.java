package com.example.kursach.controller;

import com.example.kursach.DTO.VoteRequest;
import com.example.kursach.entity.VoteValue; // Import enum
import com.example.kursach.exception.BadRequestException; // Import exception
import com.example.kursach.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService; // Service for question/answer votes

    // Vote for a question
    @PostMapping("/questions/{questionId}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> voteQuestion(@PathVariable Long questionId,
                                                @Valid @RequestBody VoteRequest voteRequest) {
        VoteValue voteValue = parseVoteValue(voteRequest.getValue());
        int newVoteCount = voteService.voteQuestion(questionId, voteValue);
        return ResponseEntity.ok(newVoteCount);
    }

    // Vote for an answer
    @PostMapping("/answers/{answerId}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> voteAnswer(@PathVariable Long answerId,
                                              @Valid @RequestBody VoteRequest voteRequest) {
        VoteValue voteValue = parseVoteValue(voteRequest.getValue());
        int newVoteCount = voteService.voteAnswer(answerId, voteValue);
        return ResponseEntity.ok(newVoteCount);
    }

    // Helper to parse request value to Enum
    private VoteValue parseVoteValue(Integer value) {
        if (value == null) {
            throw new BadRequestException("Значение голоса не может быть пустым.");
        }
        if (value == 1) {
            return VoteValue.UPVOTE;
        } else if (value == -1) {
            return VoteValue.DOWNVOTE;
        } else {
            throw new BadRequestException("Недопустимое значение голоса: " + value + ". Допустимы 1 или -1.");
        }
    }
}