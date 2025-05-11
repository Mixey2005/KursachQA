package com.example.kursach.repository;

import com.example.kursach.entity.Answer;
import com.example.kursach.entity.AnswerVote;
import com.example.kursach.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AnswerVoteRepository extends JpaRepository<AnswerVote, Long> {
    Optional<AnswerVote> findByUserAndAnswer(User user, Answer answer);
    long countByAnswerAndValue(Answer answer, com.example.kursach.entity.VoteValue value);
}