package com.example.kursach.service.impl; // Убедитесь, что пакет правильный

import com.example.kursach.DTO.CommentDto; // Используйте ваш пакет DTO
import com.example.kursach.entity.*;
import com.example.kursach.exception.AccessDeniedException; // Используйте ваш пакет exception
import com.example.kursach.exception.BadRequestException;   // Используйте ваш пакет exception
import com.example.kursach.exception.ResourceNotFoundException; // Используйте ваш пакет exception
import com.example.kursach.repository.*; // Используйте ваш пакет repository
import com.example.kursach.service.CommentService; // Импортируем ИСПРАВЛЕННЫЙ интерфейс
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort; // Импортируем Sort
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService { // Реализуем ИСПРАВЛЕННЫЙ интерфейс

    private final CommentRepository commentRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    // Убираем CommentVoteRepository, т.к. голосование здесь не обрабатывается

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForQuestion(Long questionId) {
        // Проверяем, существует ли вопрос
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Вопрос", "id", questionId);
        }
        // Используем findByQuestionId с Sort
        List<Comment> comments = commentRepository.findByQuestionId(questionId, Sort.by(Sort.Direction.ASC, "createdAt"));
        return comments.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForAnswer(Long answerId) {
        // Проверяем, существует ли ответ
        if (!answerRepository.existsById(answerId)) {
            throw new ResourceNotFoundException("Ответ", "id", answerId);
        }
        // Используем findByAnswerId с Sort
        List<Comment> comments = commentRepository.findByAnswerId(answerId, Sort.by(Sort.Direction.ASC, "createdAt"));
        return comments.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addCommentToQuestion(Long questionId, CommentDto commentDto) {
        User author = getCurrentUser();
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос", "id", questionId));

        // Используем конструктор и сеттеры
        Comment comment = new Comment();
        comment.setBody(commentDto.getBody());
        comment.setAuthor(author);
        comment.setQuestion(question);
        comment.setAnswer(null); // Явно указываем null для связи с ответом
        comment.setVoteCount(0); // Инициализируем счетчик голосов (если он есть в Comment)

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto addCommentToAnswer(Long answerId, CommentDto commentDto) {
        User author = getCurrentUser();
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ответ", "id", answerId));

        // Используем конструктор и сеттеры
        Comment comment = new Comment();
        comment.setBody(commentDto.getBody());
        comment.setAuthor(author);
        comment.setAnswer(answer);
        comment.setQuestion(null); // Явно указываем null для связи с вопросом
        comment.setVoteCount(0); // Инициализируем счетчик голосов (если он есть в Comment)

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Комментарий", "id", commentId));

        // Проверка прав: автор комментария ИЛИ админ
        if (!comment.getAuthor().equals(currentUser) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Вы не можете удалить этот комментарий."); // Используем ваш AccessDeniedException
        }

        // Удаляем комментарий (связанные голоса удалятся каскадно, если настроено CascadeType.ALL в Comment)
        commentRepository.delete(comment);
    }

    // --- Вспомогательные методы ---

    /**
     * Преобразует сущность Comment в CommentDto.
     * @param comment Сущность комментария
     * @return DTO комментария
     */
    private CommentDto mapToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setBody(comment.getBody());
        dto.setAuthorUsername(comment.getAuthor().getUsername());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setVoteCount(comment.getVoteCount()); // Маппим voteCount, если он есть

        // Устанавливаем ID родительского элемента
        if (comment.getQuestion() != null) {
            dto.setQuestionId(comment.getQuestion().getId());
        }
        if (comment.getAnswer() != null) {
            dto.setAnswerId(comment.getAnswer().getId());
        }

        // Опционально: можно добавить логику для определения currentUserVote,
        // но для этого потребуется CommentVoteRepository и текущий пользователь.
        // dto.setCurrentUserVote(determineCurrentUserVote(comment, getCurrentUserOpt()));

        return dto;
    }

    /**
     * Получает текущего аутентифицированного пользователя.
     * Выбрасывает исключение, если пользователь не аутентифицирован.
     * @return Сущность User
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Требуется аутентификация для этого действия."); // Используем ваш AccessDeniedException
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "username", username));
    }

    /*
    // Опциональный метод для получения пользователя без выбрасывания исключения (если нужен для mapToDto)
    private User getCurrentUserOpt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
    */

}