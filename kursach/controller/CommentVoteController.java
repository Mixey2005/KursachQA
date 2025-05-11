package com.example.kursach.controller;

import com.example.kursach.DTO.VoteRequest;
import com.example.kursach.entity.VoteValue; // Import enum
import com.example.kursach.exception.BadRequestException; // Import exception
import com.example.kursach.service.CommentVoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments/{commentId}/vote") // Route for comment votes
@RequiredArgsConstructor
public class CommentVoteController {

    private final CommentVoteService commentVoteService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> voteComment(@PathVariable Long commentId,
                                               @Valid @RequestBody VoteRequest voteRequest) {
        VoteValue voteValue = parseVoteValue(voteRequest.getValue()); // Use helper
        int newVoteCount = commentVoteService.vote(commentId, voteValue); // Pass enum
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