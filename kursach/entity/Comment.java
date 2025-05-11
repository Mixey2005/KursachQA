package com.example.kursach.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_user_id", columnList = "user_id"),
        @Index(name = "idx_comment_question_id", columnList = "question_id"),
        @Index(name = "idx_comment_answer_id", columnList = "answer_id")
})
public class Comment extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id") // Nullable
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id") // Nullable
    private Answer answer;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CommentVote> votes = new HashSet<>();

    @Column(name = "vote_count", nullable = false, columnDefinition = "integer default 0")
    private int voteCount = 0;

    // DB constraint CHECK (question_id IS NOT NULL OR answer_id IS NOT NULL) handles validation
}