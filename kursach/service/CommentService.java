package com.example.kursach.service; // Убедитесь, что пакет правильный

import com.example.kursach.DTO.CommentDto; // Используйте ваш пакет DTO
import java.util.List;

/**
 * Сервис для управления комментариями (CRUD операции).
 * Логика голосования вынесена в CommentVoteService.
 */
public interface CommentService {

    /**
     * Получить все комментарии для указанного вопроса.
     * @param questionId ID вопроса
     * @return Список DTO комментариев, отсортированных по дате создания.
     */
    List<CommentDto> getCommentsForQuestion(Long questionId);

    /**
     * Получить все комментарии для указанного ответа.
     * @param answerId ID ответа
     * @return Список DTO комментариев, отсортированных по дате создания.
     */
    List<CommentDto> getCommentsForAnswer(Long answerId);

    /**
     * Добавить новый комментарий к вопросу.
     * @param questionId ID вопроса
     * @param commentDto DTO с данными нового комментария
     * @return DTO созданного комментария.
     */
    CommentDto addCommentToQuestion(Long questionId, CommentDto commentDto);

    /**
     * Добавить новый комментарий к ответу.
     * @param answerId ID ответа
     * @param commentDto DTO с данными нового комментария
     * @return DTO созданного комментария.
     */
    CommentDto addCommentToAnswer(Long answerId, CommentDto commentDto);

    /**
     * Удалить комментарий по ID.
     * Права доступа проверяются внутри реализации.
     * @param commentId ID комментария для удаления.
     */
    void deleteComment(Long commentId);

}