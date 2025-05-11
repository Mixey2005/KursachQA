// В файле: src/main/java/com/example/kursach/service/strategy/QuestionSortByDateStrategy.java
package com.example.kursach.service.strategy;

import com.example.kursach.entity.Question;
import java.util.List;

// Класс должен реализовывать интерфейс с нужным типом
public class QuestionSortByDateStrategy implements SortingStrategy<Question> {

    @Override
    public void sort(List<Question> items) {
        // Сортировка по убыванию даты создания
        items.sort((q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt()));
    }

    // Если ваш интерфейс SortingStrategy требует метод getComparator():
    /*
    @Override
    public java.util.Comparator<Question> getComparator() {
        return (q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt());
    }
    */
}