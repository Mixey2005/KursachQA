// В файле: src/main/java/com/example/kursach/service/TranslationService.java
package com.example.kursach.service;

import com.example.kursach.DTO.TranslationRequest; // Исправьте путь к DTO если нужно
import com.example.kursach.DTO.TranslationResponse;
import reactor.core.publisher.Mono;

public interface TranslationService { // Должно быть interface, не class

    Mono<TranslationResponse> translateText(TranslationRequest request); // Объявление метода

}