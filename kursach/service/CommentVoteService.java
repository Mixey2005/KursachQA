package com.example.kursach.service;

import com.example.kursach.entity.VoteValue; // <-- Импорт Enum

public interface CommentVoteService {
    /**
     * Обрабатывает голос пользователя за комментарий.
     * @param commentId ID комментария
     * @param voteValue Значение голоса (UPVOTE или DOWNVOTE). <-- ИЗМЕНЕН ТИП
     * @return Новый суммарный счетчик голосов комментария.
     */
    int vote(Long commentId, VoteValue voteValue); // <-- ИЗМЕНЕН ТИП ПАРАМЕТРА
}