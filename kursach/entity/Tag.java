package com.example.kursach.entity; // Убедитесь, что пакет правильный

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor; // Добавлен, если нужен конструктор со всеми полями


import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Добавлен для удобства (можно убрать, если не нужен)
// НЕ ИСПОЛЬЗУЕМ @Data или @EqualsAndHashCode здесь, чтобы не переопределять логику BaseEntity
@Entity
@Table(name = "tags")
public class Tag extends BaseEntity { // Убедитесь, что наследуется от вашего BaseEntity

    @Column(nullable = false, unique = true, length = 50) // Ограничим длину имени тега
    private String name;

    // Связь с вопросами (владелец связи - Question)
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Question> questions = new HashSet<>();

    // Конструктор для удобного создания по имени (если AllArgsConstructor не используется)
    public Tag(String name) {
        this.name = name;
    }

    // equals и hashCode наследуются от BaseEntity!
}