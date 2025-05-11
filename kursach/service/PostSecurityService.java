package com.example.kursach.service;

import com.example.kursach.entity.*;
import com.example.kursach.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("postSecurityService") // Имя бина для использования в @PreAuthorize
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostSecurityService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository; // Нужен для получения текущего пользователя

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        // Оптимизация: Можно кешировать пользователя в запросе, если он часто нужен
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Текущий пользователь не найден: " + username));
    }

    public boolean isQuestionOwner(Long questionId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        return questionRepository.findById(questionId)
                .map(Question::getAuthor)
                .map(author -> author.equals(currentUser))
                .orElse(false); // Если вопрос не найден, владелец не совпадает
    }

    public boolean isAnswerOwner(Long answerId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        return answerRepository.findById(answerId)
                .map(Answer::getAuthor)
                .map(author -> author.equals(currentUser))
                .orElse(false);
    }

    public boolean isCommentOwner(Long commentId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        return commentRepository.findById(commentId)
                .map(Comment::getAuthor)
                .map(author -> author.equals(currentUser))
                .orElse(false);
    }

    // Проверка, является ли пользователь автором ВОПРОСА, к которому относится ответ
    public boolean isAuthorOfQuestionForAnswer(Long answerId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        return answerRepository.findById(answerId)
                .map(Answer::getQuestion)
                .map(Question::getAuthor)
                .map(qAuthor -> qAuthor.equals(currentUser))
                .orElse(false);
    }
}