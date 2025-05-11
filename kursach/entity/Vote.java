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
@Table(name = "votes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_question_vote", columnNames = {"user_id", "question_id"}),
        @UniqueConstraint(name = "uq_user_answer_vote", columnNames = {"user_id", "answer_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id") // Nullable
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id") // Nullable
    private Answer answer;

    // Use VoteValue Enum, but map its score to the 'score' column
    @Transient // Don't map the enum itself directly if column type is INT
    private VoteValue value;

    // Map the numeric score to the DB column
    @Column(name = "score", nullable = false)
    private int score; // Matches DB: score INT NOT NULL

    // Getter/Setter for value that synchronize with score
    public VoteValue getValue() {
        return VoteValue.fromScore(this.score);
    }

    public void setValue(VoteValue value) {
        this.value = value;
        this.score = (value != null) ? value.getScore() : 0; // Store numeric score
    }

    // Auditing fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false) // removed default here, let Auditing handle it
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // Constructors still useful
    public Vote(User user, Question question, VoteValue value) {
        this.user = user;
        this.question = question;
        this.answer = null;
        setValue(value); // Use setter to sync score
    }

    public Vote(User user, Answer answer, VoteValue value) {
        this.user = user;
        this.question = null;
        this.answer = answer;
        setValue(value); // Use setter to sync score
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vote vote = (Vote) o;
        return Objects.equals(user, vote.user) &&
                Objects.equals(question, vote.question) &&
                Objects.equals(answer, vote.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, question, answer);
    }
}