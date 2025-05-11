package com.example.kursach.service;

import com.example.kursach.DTO.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserDto> getAllUsers(Pageable pageable);
    UserDto getUserById(Long userId);
    void deleteUser(Long userId);
    UserDto getCurrentUserDto();

    // --- ДОБАВЛЕНО ---
    /**
     * Блокирует пользователя.
     * @param userId ID пользователя.
     */
    void blockUser(Long userId);

    /**
     * Разблокирует пользователя.
     * @param userId ID пользователя.
     */
    void unblockUser(Long userId);
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---
}