package com.example.kursach.repository;

import com.example.kursach.entity.Comment;
import com.example.kursach.entity.CommentVote;
import com.example.kursach.entity.User;
import com.example.kursach.entity.VoteValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {

    Optional<CommentVote> findByUserAndComment(User user, Comment comment);

    // Note: Enum value mapped to 'vote_type' column which is STRING
    long countByCommentAndValue(Comment comment, VoteValue value);
}