package com.example.kursach.service.impl;

import com.example.kursach.entity.*;
import com.example.kursach.exception.AccessDeniedException;
import com.example.kursach.exception.BadRequestException;
import com.example.kursach.exception.ResourceNotFoundException;
import com.example.kursach.repository.CommentRepository;
import com.example.kursach.repository.CommentVoteRepository;
import com.example.kursach.repository.UserRepository;
import com.example.kursach.service.CommentVoteService;
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
public class CommentVoteServiceImpl implements CommentVoteService {

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public int vote(Long commentId, VoteValue newVoteValue) { // <-- ПРИНИМАЕТ VoteValue
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Комментарий", "id", commentId));

        if (comment.getAuthor().equals(currentUser)) {
            throw new BadRequestException("Вы не можете голосовать за свой собственный комментарий.");
        }

        // Парсить voteValue больше не нужно, он уже правильного типа Enum
        // VoteValue newVoteValue = parseVoteValue(voteValueInt); // <-- УДАЛИТЬ ЭТУ СТРОКУ

        Optional<CommentVote> existingVoteOpt = commentVoteRepository.findByUserAndComment(currentUser, comment);

        if (existingVoteOpt.isPresent()) {
            CommentVote existingVote = existingVoteOpt.get();
            if (existingVote.getValue() == newVoteValue) { // Сравниваем Enum
                commentVoteRepository.delete(existingVote);
                log.debug("Пользователь {} отменил голос {} за комментарий {}", currentUser.getUsername(), newVoteValue, commentId);
            } else {
                existingVote.setValue(newVoteValue); // Устанавливаем Enum
                commentVoteRepository.save(existingVote);
                log.debug("Пользователь {} изменил голос на {} за комментарий {}", currentUser.getUsername(), newVoteValue, commentId);
            }
        } else {
            CommentVote newVote = new CommentVote(currentUser, comment, newVoteValue); // Используем Enum
            commentVoteRepository.save(newVote);
            log.debug("Пользователь {} проголосовал {} за комментарий {}", currentUser.getUsername(), newVoteValue, commentId);
        }

        int updatedVoteCount = calculateCommentVoteCount(comment);
        comment.setVoteCount(updatedVoteCount);
        commentRepository.save(comment);

        return updatedVoteCount;
    }

    private int calculateCommentVoteCount(Comment comment) {
        long upvotes = commentVoteRepository.countByCommentAndValue(comment, VoteValue.UPVOTE);
        long downvotes = commentVoteRepository.countByCommentAndValue(comment, VoteValue.DOWNVOTE);
        return (int) (upvotes - downvotes);
    }

    // Метод parseVoteValue больше не нужен здесь, т.к. парсинг происходит в контроллере
    /*
    private VoteValue parseVoteValue(Integer value) { ... }
    */

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