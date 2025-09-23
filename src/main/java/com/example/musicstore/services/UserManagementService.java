package com.example.musicstore.services;

import com.example.musicstore.models.User;
import com.example.musicstore.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления пользователями
 * Обеспечивает операции по работе с пользователями системы
 */
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;

    /**
     * Получение всех пользователей, отсортированных по ID в порядке возрастания
     * @return список всех пользователей системы
     */
    public List<User> findAllUsers() {
        return userRepository.findAllByOrderByIdAsc();
    }

    /**
     * Обновление статуса активности пользователя
     * Транзакционный метод для изменения статуса активации пользователя
     * @param userId идентификатор пользователя
     * @param isActive новый статус активности (true - активен, false - неактивен)
     * @throws RuntimeException если пользователь с указанным ID не найден
     */
    @Transactional
    public void updateUserActiveStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setActive(isActive);
        userRepository.save(user);
    }

    /**
     * Поиск пользователя по идентификатору
     * @param id идентификатор пользователя
     * @return найденный пользователь
     * @throws RuntimeException если пользователь с указанным ID не найден
     */
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}