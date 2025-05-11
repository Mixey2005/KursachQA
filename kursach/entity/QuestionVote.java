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
@Table(name = "question_votes",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "question_id"}) }) // Пользователь может голосовать за вопрос только один раз
@EntityListeners(AuditingEntityListener.class) // Для createdAt
public class QuestionVote { // Не наследуем BaseEntity, чтобы иметь композитный ключ или свой ID, но можно и наследовать

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Простой ID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.ORDINAL) // Храним как число (1 или -1)
    @Column(nullable = false)
    private VoteValue value;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public QuestionVote(User user, Question question, VoteValue value) {
        this.user = user;
        this.question = question;
        this.value = value;
    }

    // equals/hashCode важны, если работаем с сетами и этими объектами до сохранения
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionVote that = (QuestionVote) o;
        // Сравниваем по логическому ключу (пользователь, вопрос)
        return Objects.equals(user, that.user) &&
                Objects.equals(question, that.question);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, question);
    }
}