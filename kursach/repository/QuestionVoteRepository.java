package com.example.kursach.repository;

import com.example.kursach.entity.Question;
import com.example.kursach.entity.QuestionVote;
import com.example.kursach.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface QuestionVoteRepository extends JpaRepository<QuestionVote, Long> {
    // Найти голос конкретного пользователя за конкретный вопрос
    Optional<QuestionVote> findByUserAndQuestion(User user, Question question);

    // Подсчет голосов (можно использовать и для обновления voteCount)
    // @Query("SELECT SUM(v.value) FROM QuestionVote v WHERE v.question = :question") // Неправильно, value это enum
    // Лучше считать в сервисе или использовать count + group by

    long countByQuestionAndValue(Question question, com.example.kursach.entity.VoteValue value);
}