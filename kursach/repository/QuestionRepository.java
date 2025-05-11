package com.example.kursach.repository;

import com.example.kursach.entity.Question;
import com.example.kursach.entity.Tag;
import com.example.kursach.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph; // Импорт
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional; // Импорт Optional

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // --- ОПТИМИЗАЦИЯ N+1 ---
    // Используем EntityGraph для загрузки автора и тегов вместе со списком вопросов
    @EntityGraph(attributePaths = {"author", "tags"})
    @Override
    Page<Question> findAll(Pageable pageable);

    // Используем EntityGraph для загрузки всего необходимого при поиске по ID
    @EntityGraph(attributePaths = {
            "author", "tags", "answers", "comments",
            "answers.author", "comments.author", "acceptedAnswer"
            // Если нужны голоса или комменты к ответам/комментам - добавить и их
            // "votes", "answers.votes", "answers.comments", "answers.comments.author"
            // "comments.votes"
    })
    @Override
    Optional<Question> findById(Long id);
    // --- КОНЕЦ ОПТИМИЗАЦИИ ---

    // Остальные кастомные методы, если они есть
    @EntityGraph(attributePaths = {"author", "tags"}) // Тоже оптимизируем
    Page<Question> findByAuthor(User author, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "tags"}) // Тоже оптимизируем
    Page<Question> findByTags(Tag tag, Pageable pageable);

    @Query("SELECT q FROM Question q JOIN q.tags t WHERE lower(q.title) LIKE lower(concat('%', :searchTerm, '%')) OR lower(q.body) LIKE lower(concat('%', :searchTerm, '%')) OR lower(t.name) = lower(:searchTerm)")
    @EntityGraph(attributePaths = {"author", "tags"}) // Тоже оптимизируем
    Page<Question> searchByTitleOrBodyOrTag(String searchTerm, Pageable pageable);
}