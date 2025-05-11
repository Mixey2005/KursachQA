package com.example.kursach.service; // Interface VoteService.java

import com.example.kursach.entity.VoteValue;

public interface VoteService {
    int voteQuestion(Long questionId, VoteValue voteValue);
    int voteAnswer(Long answerId, VoteValue voteValue);
}