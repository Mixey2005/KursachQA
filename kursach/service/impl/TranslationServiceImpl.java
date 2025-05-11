package com.example.kursach.service.impl;

import com.example.kursach.DTO.TranslationRequest;
import com.example.kursach.DTO.TranslationResponse;
import com.example.kursach.service.TranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
// Добавлен импорт ParameterizedTypeReference
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List; // Импорт List
import java.util.Map;

@Slf4j
@Service
public class TranslationServiceImpl implements TranslationService {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;

    // Используем конструктор для внедрения зависимостей
    public TranslationServiceImpl(WebClient.Builder webClientBuilder,
                                  @Value("${google.translate.api.key:YOUR_API_KEY_PLACEHOLDER}") String apiKey, // Заглушка для ключа
                                  @Value("${google.translate.api.url:https://translation.googleapis.com/language/translate/v2}") String apiUrl) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        log.info("Google Translate API Key: {}", apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_API_KEY_PLACEHOLDER") ? "Loaded" : "MISSING or using placeholder!");
        log.info("Google Translate API URL: {}", apiUrl);
    }

    @Override // Теперь метод есть в интерфейсе
    public Mono<TranslationResponse> translateText(TranslationRequest request) {
        // Геттеры теперь есть благодаря @Data в TranslationRequest
        log.debug("Запрос на перевод: текст='{}', цель='{}', источник='{}'",
                request.getText(), request.getTargetLanguage(), request.getSourceLanguage());

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_PLACEHOLDER")) {
            log.error("API ключ Google Translate не настроен или используется заглушка!");
            // Возвращаем ошибку или заглушку
            // Конструктор TranslationResponse(String, String) теперь есть
            return Mono.just(new TranslationResponse("Ошибка: API ключ не настроен.", request.getSourceLanguage()));
            // return Mono.error(new IllegalStateException("API ключ Google Translate не настроен!"));
        }

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .bodyValue(Map.of(
                        "q", request.getText(),
                        "target", request.getTargetLanguage(),
                        "source", request.getSourceLanguage() != null ? request.getSourceLanguage() : ""
                ))
                .retrieve()
                // Используем ParameterizedTypeReference для избежания unchecked warning
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(responseMap -> {
                    log.debug("Ответ от Google Translate API: {}", responseMap);
                    try {
                        // Каст все еще нужен, но мы знаем тип из ParameterizedTypeReference
                        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                        // Каст все еще нужен
                        List<Map<String, String>> translations = (List<Map<String, String>>) data.get("translations");

                        if (translations != null && !translations.isEmpty()) {
                            String translatedText = translations.get(0).get("translatedText");
                            String detectedSourceLanguage = translations.get(0).get("detectedSourceLanguage");
                            // Конструктор TranslationResponse(String, String) теперь есть
                            return new TranslationResponse(translatedText, detectedSourceLanguage);
                        } else {
                            log.warn("Не удалось найти перевод в ответе API: {}", responseMap);
                            return new TranslationResponse("Ошибка: Не удалось получить перевод.", request.getSourceLanguage());
                        }
                    } catch (ClassCastException | NullPointerException e) { // Ловим возможные ошибки каста/NPE
                        log.error("Ошибка парсинга ответа Google Translate API: {}", responseMap, e);
                        return new TranslationResponse("Ошибка: Неверный формат ответа API.", request.getSourceLanguage());
                    }
                })
                .onErrorResume(e -> {
                    log.error("Ошибка при вызове Google Translate API", e);
                    return Mono.just(new TranslationResponse("Ошибка: Не удалось связаться с сервисом перевода.", request.getSourceLanguage()));
                });
    }
}