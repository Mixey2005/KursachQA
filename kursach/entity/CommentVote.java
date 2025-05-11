package com.example.kursach.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comment_votes",
        uniqueConstraints = { @UniqueConstraint(name = "uk_user_comment_vote", columnNames = {"user_id", "comment_id"}) })
@EntityListeners(AuditingEntityListener.class)
public class CommentVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use IDENTITY for BIGSERIAL
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    // Map Enum by STRING to match VARCHAR 'vote_type' column
    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 10) // Match DB column
    private VoteValue value;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    public CommentVote(User user, Comment comment, VoteValue value) {
        this.user = user;
        this.comment = comment;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentVote that = (CommentVote) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, comment);
    }
}