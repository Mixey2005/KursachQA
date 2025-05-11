package com.example.kursach.service;

import com.example.kursach.DTO.AnswerDto;
import com.example.kursach.DTO.QuestionDto;
import com.example.kursach.DTO.QuestionUpdateRequest; // <-- ИМПОРТ
import com.example.kursach.entity.Question;
import com.example.kursach.service.strategy.SortingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionService {

    Page<QuestionDto> getAllQuestions(Pageable pageable);

    QuestionDto getQuestionById(Long id);

    QuestionDto createQuestion(QuestionDto questionDto); // При создании можем ожидать полный DTO

    // --- ИЗМЕНЕНО ---
    /**
     * Обновляет существующий вопрос.
     * @param questionId ID вопроса для обновления.
     * @param updateRequest DTO с данными для обновления (title, body, tagNames).
     * @return DTO обновленного вопроса.
     */
    QuestionDto updateQuestion(Long questionId, QuestionUpdateRequest updateRequest);
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---

    void deleteQuestion(Long questionId);

    AnswerDto markAnswerAsAccepted(Long questionId, Long answerId);

    // Оставляем метод для стратегии, раз он есть в реализации
    Page<QuestionDto> getQuestionsSorted(Pageable pageable, SortingStrategy<Question> strategy);
}