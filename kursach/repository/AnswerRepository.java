package com.example.kursach.repository;

import com.example.kursach.entity.Answer;
import com.example.kursach.entity.Question;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // Найти все ответы для конкретного вопроса, с сортировкой
    List<Answer> findByQuestion(Question question, Sort sort);
    List<Answer> findByQuestionId(Long questionId, Sort sort);
}