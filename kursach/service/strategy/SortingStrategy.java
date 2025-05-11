package com.example.kursach.service.strategy; // Убедитесь, что пакет правильный

import java.util.List; // Или другой тип коллекции

@FunctionalInterface // Можно сделать функциональным, если только один метод
public interface SortingStrategy<T> { // Добавлен <T>
    void sort(List<T> items); // Используется T
    // Или: java.util.Comparator<T> getComparator();
}