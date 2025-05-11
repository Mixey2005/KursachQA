package com.example.kursach.service.impl;

import com.example.kursach.DTO.UserDto;
import com.example.kursach.entity.User;
import com.example.kursach.exception.AccessDeniedException;
import com.example.kursach.exception.ResourceNotFoundException;
import com.example.kursach.repository.UserRepository;
import com.example.kursach.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapUserToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user = findUserById(userId);
        return mapUserToDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User userToDelete = findUserById(userId);
        // TODO: Consider implications before deleting (reassign content?)
        userRepository.delete(userToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserDto() {
        User user = getCurrentUserInternal();
        return mapUserToDto(user);
    }

    // --- ДОБАВЛЕНО: Реализация block/unblock ---
    @Override
    @Transactional
    public void blockUser(Long userId) {
        User user = findUserById(userId);
        user.setLocked(true); // Устанавливаем флаг блокировки
        userRepository.save(user);
        // TODO: Возможно, нужно инвалидировать активные сессии/токены пользователя
    }

    @Override
    @Transactional
    public void unblockUser(Long userId) {
        User user = findUserById(userId);
        user.setLocked(false); // Снимаем флаг блокировки
        userRepository.save(user);
    }
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---


    // --- Вспомогательные методы ---
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "id", userId));
    }

    private User getCurrentUserInternal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Пользователь не аутентифицирован");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Текущий пользователь", "username", username + " (не найден в БД)"));
    }

    // --- Обновленный маппер ---
    private UserDto mapUserToDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }
        dto.setEnabled(user.isEnabled()); // Добавлено
        dto.setLocked(user.isLocked());   // Добавлено
        // dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}