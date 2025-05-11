package com.example.kursach.service.impl;

// --- Импорты ---
import com.example.kursach.DTO.AnswerDto;
import com.example.kursach.DTO.CommentDto;
import com.example.kursach.DTO.QuestionDto;
import com.example.kursach.DTO.QuestionUpdateRequest;
import com.example.kursach.entity.*;
import com.example.kursach.exception.AccessDeniedException;
import com.example.kursach.exception.BadRequestException;
import com.example.kursach.exception.ResourceNotFoundException;
import com.example.kursach.repository.*;
import com.example.kursach.service.PostSecurityService;
import com.example.kursach.service.QuestionService;
import com.example.kursach.service.strategy.SortingStrategy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // <-- Импорт логгера
import org.slf4j.LoggerFactory; // <-- Импорт логгера
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
// --- Конец импортов ---

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    // --- Добавляем логгер ---
    private static final Logger log = LoggerFactory.getLogger(QuestionServiceImpl.class);
    // ---

    // --- Репозитории и сервисы ---
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final PostSecurityService postSecurityService;

    // --- createQuestion с логированием ---
    @Override
    @Transactional
    public QuestionDto createQuestion(QuestionDto questionDto) {
        User author = getCurrentUser();
        log.info("Создание вопроса пользователем: {}", author.getUsername());
        log.debug("Получен DTO: {}", questionDto);

        Question question = new Question();
        question.setTitle(questionDto.getTitle());
        question.setBody(questionDto.getBody());
        question.setAuthor(author);
        question.setVoteCount(0);

        try {
            Set<Tag> tags = processTags(questionDto.getTagNames());
            question.setTags(tags);
            log.debug("Теги обработаны: {}", tags.stream().map(Tag::getName).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Ошибка при обработке тегов", e);
            throw new RuntimeException("Ошибка обработки тегов", e);
        }

        try {
            Question savedQuestion = questionRepository.save(question);
            log.info("Вопрос сохранен с ID: {}", savedQuestion.getId());
            return mapToDto(savedQuestion, author);
        } catch (Exception e) {
            log.error("Ошибка при сохранении вопроса в БД", e);
            throw new RuntimeException("Не удалось сохранить вопрос", e);
        }
    }

    // --- getAllQuestions с логированием (ЕДИНСТВЕННАЯ реализация) ---
    @Override
    @Transactional(readOnly = true)
    public Page<QuestionDto> getAllQuestions(Pageable pageable) {
        User currentUser = getCurrentUserOpt();
        String usernameForLog = currentUser != null ? currentUser.getUsername() : "Anonymous";
        log.info("Запрос списка вопросов для пользователя '{}'. Страница: {}, Размер: {}, Сортировка: {}",
                usernameForLog, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<Question> questionPage = questionRepository.findAll(pageable);
            log.debug("Найдено вопросов в БД: {}", questionPage.getTotalElements());
            Page<QuestionDto> dtoPage = questionPage.map(question -> mapToDto(question, currentUser));
            log.debug("Возвращается страница DTO: {} элементов", dtoPage.getNumberOfElements());
            return dtoPage;
        } catch (Exception e) {
            log.error("Ошибка при получении списка вопросов", e);
            throw new RuntimeException("Ошибка получения списка вопросов", e);
        }
    }

    // --- Остальные методы сервиса ---
    @Override
    @Transactional(readOnly = true)
    public QuestionDto getQuestionById(Long id) {
        User currentUser = getCurrentUserOpt();
        log.debug("Запрос вопроса с ID: {} для пользователя {}", id, currentUser != null ? currentUser.getUsername() : "Anonymous");
        Question question = questionRepository.findById(id) // Использует EntityGraph из репозитория
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос", "id", id));
        return mapToDto(question, currentUser);
    }

    @Override
    @Transactional
    public QuestionDto updateQuestion(Long questionId, QuestionUpdateRequest updateRequest) {
        User currentUser = getCurrentUser();
        log.info("Попытка обновления вопроса ID: {} пользователем {}", questionId, currentUser.getUsername());
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос", "id", questionId));

        checkPermissionToModify(question.getAuthor(), currentUser); // Проверка прав

        log.debug("Обновление данных вопроса ID: {}", questionId);
        question.setTitle(updateRequest.getTitle());
        question.setBody(updateRequest.getBody());
        if (updateRequest.getTagNames() != null) {
            Set<Tag> tags = processTags(updateRequest.getTagNames());
            question.setTags(tags);
            log.debug("Теги для вопроса ID {} обновлены.", questionId);
        }

        Question updatedQuestion = questionRepository.save(question);
        log.info("Вопрос ID {} успешно обновлен.", questionId);
        return mapToDto(updatedQuestion, currentUser);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        User currentUser = getCurrentUser();
        log.warn("Попытка удаления вопроса ID: {} пользователем {}", questionId, currentUser.getUsername()); // Warn, т.к. деструктивное действие
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос", "id", questionId));

        checkPermissionToModify(question.getAuthor(), currentUser); // Проверка прав
        questionRepository.delete(question);
        log.info("Вопрос ID {} успешно удален.", questionId);
    }

    @Override
    @Transactional
    public AnswerDto markAnswerAsAccepted(Long questionId, Long answerId) {
        User currentUser = getCurrentUser();
        log.info("Попытка отметить ответ ID: {} как лучший для вопроса ID: {} пользователем {}", answerId, questionId, currentUser.getUsername());
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос", "id", questionId));
        Answer answerToAccept = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ответ", "id", answerId));

        if (!answerToAccept.getQuestion().equals(question)) {
            log.error("Ошибка принятия ответа: Ответ {} не принадлежит вопросу {}", answerId, questionId);
            throw new BadRequestException("Ответ не принадлежит этому вопросу.");
        }
        if (!question.getAuthor().equals(currentUser)) {
            log.warn("Ошибка принятия ответа: Пользователь {} не является автором вопроса {}", currentUser.getUsername(), questionId);
            throw new AccessDeniedException("Только автор вопроса может выбрать лучший ответ.");
        }

        Answer currentAccepted = question.getAcceptedAnswer();
        log.debug("Текущий принятый ответ для вопроса {}: {}", questionId, currentAccepted != null ? currentAccepted.getId() : "нет");
        if (currentAccepted != null && !currentAccepted.equals(answerToAccept)) {
            currentAccepted.setAccepted(false);
            answerRepository.save(currentAccepted);
            log.debug("Снята отметка 'принято' с ответа {}", currentAccepted.getId());
        }

        answerToAccept.setAccepted(true);
        question.setAcceptedAnswer(answerToAccept);
        Answer savedAnswer = answerRepository.save(answerToAccept); // Сохраняем ответ
        questionRepository.save(question); // Сохраняем вопрос со ссылкой
        log.info("Ответ {} успешно отмечен как лучший для вопроса {}", answerId, questionId);

        return mapAnswerToDto(savedAnswer, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionDto> getQuestionsSorted(Pageable pageable, SortingStrategy<Question> strategy) {
        // TODO: Реализовать логику со стратегией, если нужно
        log.debug("Вызван getQuestionsSorted (пока использует стандартную сортировку)");
        User currentUser = getCurrentUserOpt();
        return questionRepository.findAll(pageable).map(question -> mapToDto(question, currentUser));
    }

    // --- Маппинг и вспомогательные методы ---
    // (Без изменений, но логи внутри них теперь будут работать)
    // ... (mapToDto, mapAnswerToDto, mapCommentToDto, processTags, ...)
    // ... (getCurrentUser, getCurrentUserOpt, canModify, canModifyOrQuestionOwner, checkPermissionToModify, isLoggedIn) ...

    // Мапперы и вспомогательные методы (без изменений, но с рабочим логгером)
    private QuestionDto mapToDto(Question question, User currentUser) {
        // ... (код как в прошлой полной версии) ...
        return new QuestionDto(); // Заглушка, используйте полный код из прошлого ответа
    }
    private AnswerDto mapAnswerToDto(Answer answer, User currentUser) {
        // ... (код как в прошлой полной версии) ...
        return new AnswerDto(); // Заглушка, используйте полный код из прошлого ответа
    }
    private CommentDto mapCommentToDto(Comment comment, User currentUser) {
        // ... (код как в прошлой полной версии) ...
        return new CommentDto(); // Заглушка, используйте полный код из прошлого ответа
    }
    private Set<Tag> processTags(Set<String> tagNames) {
        // ... (код как в прошлой полной версии) ...
        return new HashSet<>(); // Заглушка, используйте полный код из прошлого ответа
    }
    private User getCurrentUser() {
        // ... (код как в прошлой полной версии) ...
        return null; // Заглушка, используйте полный код из прошлого ответа
    }
    private User getCurrentUserOpt() {
        // ... (код как в прошлой полной версии) ...
        return null; // Заглушка, используйте полный код из прошлого ответа
    }
    private boolean isLoggedIn(User currentUser) { return currentUser != null; }
    private boolean canModify(User postAuthor, User currentUser) {
        // ... (код как в прошлой полной версии) ...
        return false; // Заглушка
    }
    private boolean canModifyOrQuestionOwner(Answer answer, User currentUser) {
        // ... (код как в прошлой полной версии) ...
        return false; // Заглушка
    }
    private void checkPermissionToModify(User postAuthor, User currentUser) {
        // ... (код как в прошлой полной версии) ...
    }


} // Конец класса