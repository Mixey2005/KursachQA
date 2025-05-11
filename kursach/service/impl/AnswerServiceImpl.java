package com.example.kursach.service.impl;

import com.example.kursach.DTO.AnswerDto;
import com.example.kursach.DTO.AnswerUpdateRequest; // <-- Импорт
import com.example.kursach.DTO.CommentDto;
import com.example.kursach.entity.*;
import com.example.kursach.exception.AccessDeniedException;
import com.example.kursach.exception.BadRequestException; // Может понадобиться
import com.example.kursach.exception.ResourceNotFoundException;
import com.example.kursach.repository.*;
import com.example.kursach.service.AnswerService;
// Импорт PostSecurityService может быть полезен для сложных проверок
// import com.example.kursach.service.PostSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Objects; // Для Objects::nonNull
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository; // Для определения currentUserVote
    private final CommentVoteRepository commentVoteRepository; // Для комментов к ответам
    // private final PostSecurityService postSecurityService; // Если используется

    // --- getAnswersForQuestion, addAnswer (без существенных изменений, кроме маппинга) ---
    @Override
    @Transactional(readOnly = true)
    public List<AnswerDto> getAnswersForQuestion(Long questionId) {
        User currentUser = getCurrentUserOpt(); // Получаем текущего пользователя
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Вопрос", "id", questionId);
        }
        Sort sort = Sort.by(Sort.Order.desc("accepted"), Sort.Order.desc("voteCount"), Sort.Order.asc("createdAt"));
        List<Answer> answers = answerRepository.findByQuestionId(questionId, sort);
        return answers.stream()
                .map(answer -> mapToDto(answer, currentUser)) // Передаем пользователя
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnswerDto addAnswer(Long questionId, AnswerDto answerDto) { // Пока оставляем AnswerDto
        User author = getCurrentUser();
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос", "id", questionId));

        Answer answer = new Answer();
        answer.setBody(answerDto.getBody());
        answer.setQuestion(question);
        answer.setAuthor(author);
        answer.setAccepted(false);
        answer.setVoteCount(0);

        Answer savedAnswer = answerRepository.save(answer);
        // TODO: Уведомление автору вопроса
        return mapToDto(savedAnswer, author); // Передаем автора как текущего
    }


    // --- ОБНОВЛЕН updateAnswer ---
    @Override
    @Transactional
    public AnswerDto updateAnswer(Long answerId, AnswerUpdateRequest updateRequest) {
        User currentUser = getCurrentUser();
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ответ", "id", answerId));

        // Проверка прав
        checkPermissionToModify(answer.getAuthor(), currentUser);

        answer.setBody(updateRequest.getBody());
        Answer updatedAnswer = answerRepository.save(answer);
        return mapToDto(updatedAnswer, currentUser);
    }
    // --- КОНЕЦ ОБНОВЛЕНИЯ ---

    @Override
    @Transactional
    public void deleteAnswer(Long answerId) {
        User currentUser = getCurrentUser();
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ответ", "id", answerId));

        // Проверка прав: автор ответа ИЛИ автор вопроса ИЛИ админ/модератор
        if (!canModifyOrQuestionOwner(answer, currentUser)) {
            throw new AccessDeniedException("У вас нет прав для удаления этого ответа.");
        }

        // Снять отметку "лучший ответ", если удаляется именно он
        if (answer.isAccepted()) {
            Question question = answer.getQuestion();
            question.setAcceptedAnswer(null);
            questionRepository.save(question);
        }

        answerRepository.delete(answer);
    }


    // --- Вспомогательные методы ---

    // Маппер DTO (добавляем canEdit/canDelete/canAccept)
    private AnswerDto mapToDto(Answer answer, User currentUser) {
        if (answer == null) return null;
        AnswerDto dto = new AnswerDto();
        dto.setId(answer.getId());
        dto.setBody(answer.getBody());
        dto.setQuestionId(answer.getQuestion().getId());
        dto.setAuthorUsername(answer.getAuthor().getUsername());
        dto.setVoteCount(answer.getVoteCount());
        dto.setAccepted(answer.isAccepted());
        dto.setCreatedAt(answer.getCreatedAt());
        dto.setUpdatedAt(answer.getUpdatedAt());

        // Определение голоса
        if (currentUser != null) {
            voteRepository.findByUserAndAnswer(currentUser, answer)
                    .ifPresent(vote -> dto.setCurrentUserVote(vote.getValue().getScore()));
        } else {
            dto.setCurrentUserVote(null);
        }

        // Определение прав
        dto.setCanEdit(canModify(answer.getAuthor(), currentUser));
        dto.setCanDelete(canModifyOrQuestionOwner(answer, currentUser));
        dto.setCanAccept(isLoggedIn(currentUser) && answer.getQuestion().getAuthor().equals(currentUser));


        // Маппинг комментариев
        Sort commentSort = Sort.by("createdAt");
        List<Comment> comments = commentRepository.findByAnswerId(answer.getId(), commentSort);
        dto.setComments(comments.stream()
                .map(comment -> mapCommentToDto(comment, currentUser)) // Передаем пользователя
                .collect(Collectors.toList()));

        return dto;
    }

    // Маппер комментариев (можно вынести)
    private CommentDto mapCommentToDto(Comment comment, User currentUser) {
        if (comment == null) return null;
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setBody(comment.getBody());
        dto.setAuthorUsername(comment.getAuthor().getUsername());
        dto.setAnswerId(comment.getAnswer() != null ? comment.getAnswer().getId() : null);
        dto.setQuestionId(comment.getQuestion() != null ? comment.getQuestion().getId() : null);
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setVoteCount(comment.getVoteCount());

        if (currentUser != null) {
            commentVoteRepository.findByUserAndComment(currentUser, comment)
                    .ifPresent(vote -> dto.setCurrentUserVote(vote.getValue().getScore()));
        } else {
            dto.setCurrentUserVote(null);
        }
        dto.setCanDelete(canModify(comment.getAuthor(), currentUser));
        return dto;
    }

    // --- Методы проверки прав (аналогичны QuestionServiceImpl) ---
    private boolean isLoggedIn(User currentUser) { return currentUser != null; }

    private boolean canModify(User postAuthor, User currentUser) {
        if (!isLoggedIn(currentUser)) return false;
        return postAuthor.equals(currentUser) ||
                currentUser.getRole() == Role.ADMIN ||
                currentUser.getRole() == Role.MODERATOR;
    }

    private boolean canModifyOrQuestionOwner(Answer answer, User currentUser) {
        if (!isLoggedIn(currentUser)) return false;
        return canModify(answer.getAuthor(), currentUser) ||
                answer.getQuestion().getAuthor().equals(currentUser);
    }

    private void checkPermissionToModify(User postAuthor, User currentUser) {
        if (!canModify(postAuthor, currentUser)) {
            throw new AccessDeniedException("У вас нет прав для выполнения этого действия.");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Требуется аутентификация.");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "username", username));
    }

    private User getCurrentUserOpt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}