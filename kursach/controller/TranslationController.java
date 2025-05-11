package com.example.kursach.controller;

import com.example.kursach.DTO.TranslationRequest;
import com.example.kursach.DTO.TranslationResponse;
import com.example.kursach.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/translate")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping
    public Mono<ResponseEntity<TranslationResponse>> translate(@Valid @RequestBody TranslationRequest request) {
        return translationService.translateText(request)
                .map(ResponseEntity::ok) // Если успешно, возвращаем 200 OK с результатом
                .defaultIfEmpty(ResponseEntity.notFound().build()); // Если Mono пустой (маловероятно с onErrorResume), возвращаем 404
    }
}