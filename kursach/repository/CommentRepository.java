package com.example.kursach.repository;

import com.example.kursach.entity.Comment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Используем этот метод с параметром Sort
    List<Comment> findByQuestionId(Long questionId, Sort sort);
    // Используем этот метод с параметром Sort
    List<Comment> findByAnswerId(Long answerId, Sort sort);
}