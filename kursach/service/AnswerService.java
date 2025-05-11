package com.example.kursach.service;

import com.example.kursach.DTO.AnswerDto;
import com.example.kursach.DTO.AnswerUpdateRequest; // <-- ИМПОРТ
import java.util.List;

public interface AnswerService {

    List<AnswerDto> getAnswersForQuestion(Long questionId);

    // При добавлении можно использовать и AnswerDto, и AnswerCreateRequestDto
    AnswerDto addAnswer(Long questionId, AnswerDto answerDto);

    // --- ИЗМЕНЕНО ---
    /**
     * Обновляет существующий ответ.
     * @param answerId ID ответа для обновления.
     * @param updateRequest DTO с данными для обновления (body).
     * @return DTO обновленного ответа.
     */
    AnswerDto updateAnswer(Long answerId, AnswerUpdateRequest updateRequest);
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---

    void deleteAnswer(Long answerId);

    // Метод mapToDto убран из интерфейса, т.к. он вспомогательный для реализации
}