package com.example.kursach.service.impl;

import com.example.kursach.entity.*;
import com.example.kursach.exception.AccessDeniedException;
import com.example.kursach.exception.BadRequestException;
import com.example.kursach.exception.ResourceNotFoundException;
import com.example.kursach.repository.*;
import com.example.kursach.service.VoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public int voteQuestion(Long questionId, VoteValue newVoteValue) {
        User currentUser = getCurrentUser();
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос", "id", questionId));

        if (question.getAuthor().equals(currentUser)) {
            throw new BadRequestException("Вы не можете голосовать за свой собственный вопрос.");
        }

        Optional<Vote> existingVoteOpt = voteRepository.findByUserAndQuestion(currentUser, question);

        handleVote(existingVoteOpt, currentUser, question, null, newVoteValue);

        int updatedVoteCount = calculateQuestionVoteCount(question);
        question.setVoteCount(updatedVoteCount);
        questionRepository.save(question);
        log.debug("Обновлен счетчик голосов вопроса {}: {}", questionId, updatedVoteCount);
        return updatedVoteCount;
    }

    @Override
    @Transactional
    public int voteAnswer(Long answerId, VoteValue newVoteValue) {
        User currentUser = getCurrentUser();
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ответ", "id", answerId));

        if (answer.getAuthor().equals(currentUser)) {
            throw new BadRequestException("Вы не можете голосовать за свой собственный ответ.");
        }

        Optional<Vote> existingVoteOpt = voteRepository.findByUserAndAnswer(currentUser, answer);

        handleVote(existingVoteOpt, currentUser, null, answer, newVoteValue);

        int updatedVoteCount = calculateAnswerVoteCount(answer);
        answer.setVoteCount(updatedVoteCount);
        answerRepository.save(answer);
        log.debug("Обновлен счетчик голосов ответа {}: {}", answerId, updatedVoteCount);
        return updatedVoteCount;
    }

    private void handleVote(Optional<Vote> existingVoteOpt, User user, Question question, Answer answer, VoteValue newVoteValue) {
        if (existingVoteOpt.isPresent()) {
            Vote existingVote = existingVoteOpt.get();
            if (existingVote.getValue() == newVoteValue) { // Сравнение Enum работает
                voteRepository.delete(existingVote);
                log.debug("Пользователь {} отменил голос {} за {}", user.getUsername(), newVoteValue, (question != null ? "вопрос " + question.getId() : "ответ " + answer.getId()));
            } else {
                existingVote.setValue(newVoteValue); // Используем сеттер, он обновит и score
                voteRepository.save(existingVote);
                log.debug("Пользователь {} изменил голос на {} за {}", user.getUsername(), newVoteValue, (question != null ? "вопрос " + question.getId() : "ответ " + answer.getId()));
            }
        } else {
            Vote newVote = (question != null)
                    ? new Vote(user, question, newVoteValue)
                    : new Vote(user, answer, newVoteValue);
            // score установится в конструкторе через setValue()
            voteRepository.save(newVote);
            log.debug("Пользователь {} проголосовал {} за {}", user.getUsername(), newVoteValue, (question != null ? "вопрос " + question.getId() : "ответ " + answer.getId()));
        }
    }

    // --- ИЗМЕНЕНО: Используем методы с полем 'score' ---
    private int calculateQuestionVoteCount(Question question) {
        long upvotes = voteRepository.countByQuestionAndScore(question, VoteValue.UPVOTE.getScore()); // Используем getScore()
        long downvotes = voteRepository.countByQuestionAndScore(question, VoteValue.DOWNVOTE.getScore()); // Используем getScore()
        return (int) (upvotes - downvotes);
    }

    private int calculateAnswerVoteCount(Answer answer) {
        long upvotes = voteRepository.countByAnswerAndScore(answer, VoteValue.UPVOTE.getScore()); // Используем getScore()
        long downvotes = voteRepository.countByAnswerAndScore(answer, VoteValue.DOWNVOTE.getScore()); // Используем getScore()
        return (int) (upvotes - downvotes);
    }
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Требуется аутентификация для голосования.");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "username", username));
    }
}