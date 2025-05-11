package com.example.kursach.repository;

import com.example.kursach.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByUserAndQuestion(User user, Question question);

    Optional<Vote> findByUserAndAnswer(User user, Answer answer);

    // --- ИЗМЕНЕНО: Ищем по полю 'score' ---
    /**
     * Считает количество голосов для вопроса с определенным числовым значением score.
     * @param question Вопрос
     * @param score Числовое значение голоса (1 или -1)
     * @return Количество голосов.
     */
    long countByQuestionAndScore(Question question, int score);

    /**
     * Считает количество голосов для ответа с определенным числовым значением score.
     * @param answer Ответ
     * @param score Числовое значение голоса (1 или -1)
     * @return Количество голосов.
     */
    long countByAnswerAndScore(Answer answer, int score);
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---


    // Запросы @Query остаются без изменений, так как они используют JPQL
    @Query("SELECT count(v) FROM Vote v WHERE v.question = :question AND v.score = 1") // Используем v.score = 1
    long countUpvotesForQuestion(Question question);

    @Query("SELECT count(v) FROM Vote v WHERE v.question = :question AND v.score = -1") // Используем v.score = -1
    long countDownvotesForQuestion(Question question);

    @Query("SELECT count(v) FROM Vote v WHERE v.answer = :answer AND v.score = 1") // Используем v.score = 1
    long countUpvotesForAnswer(Answer answer);

    @Query("SELECT count(v) FROM Vote v WHERE v.answer = :answer AND v.score = -1") // Используем v.score = -1
    long countDownvotesForAnswer(Answer answer);
}