package com.example.musicstore.services;

import com.example.musicstore.models.User;
import com.example.musicstore.models.enums.Role;
import com.example.musicstore.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для управления пользователями
 * Обеспечивает регистрацию, аутентификацию, управление балансом и ролями пользователей
 * Интегрирован с Spring Security для аутентификации и авторизации
 */
@Service // Аннотация указывает, что этот класс является сервисным компонентом Spring
@Slf4j // Автоматически создает логгер для ведения журнала (log.debug(), log.info() и т.д.)
@RequiredArgsConstructor // Генерирует конструктор для final-полей
// Реализует UserDetailsService для интеграции с Spring Security
public class UserService implements UserDetailsService {

    // Репозиторий для работы с пользователями в базе данных
    private final UserRepository userRepository;
    // Сервис для отправки уведомлений (например, email)
    private final NotificationService notificationService;
    // Кодировщик паролей для безопасного хранения
    private final PasswordEncoder passwordEncoder;

    /**
     * Создание нового пользователя
     * @param user объект пользователя для создания
     * @return true если пользователь успешно создан, false если пользователь уже существует
     * @throws IllegalArgumentException если email пустой или невалидный
     */
    @Transactional // Обеспечивает атомарность операции в рамках транзакции
    public boolean createUser(User user) {
        // Валидация email - проверка что email не null и не пустой
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Адрес электронной почты не может быть пустым");
        }

        // Проверка уникальности email в базе данных
        if (userRepository.existsByEmail(user.getEmail())) {
            return false; // Пользователь уже существует
        }

        // Настройка параметров нового пользователя
        user.setActive(true); // Установка статуса активного пользователя
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Хеширование пароля для безопасности
        user.setRoles(Set.of(Role.ROLE_USER)); // Назначение роли по умолчанию (обычный пользователь)

        // Сохранение пользователя в базе данных
        User savedUser = userRepository.save(user);

        // Отправка приветственного email (закомментировано, можно раскомментировать при необходимости)
        // notificationService.sendWelcomeEmail(savedUser);

        // Логирование успешной регистрации
        log.info("Пользователь успешно зарегистрировался: {}", user.getEmail());
        return true;
    }

    /**
     * Поиск пользователя по email
     * @param email email пользователя для поиска
     * @return Optional с пользователем, если найден, или пустой Optional
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Метод для аутентификации пользователя через Spring Security
     * @param email email пользователя (используется как username)
     * @return UserDetails объект для Spring Security
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Поиск пользователя в базе данных по email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден по электронной почте: " + email));

        // Создание UserDetails объекта для Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),  // username (используем email)
                user.getPassword(), // хэшированный пароль
                user.isActive(),    // аккаунт активен?
                true, // аккаунт не expired (не просрочен)
                true, // аккаунт не locked (не заблокирован)
                true, // credentials не expired (учетные данные не просрочены)
                user.getAuthorities()); // роли и权限 пользователя
    }

    /**
     * Удаление пользователя по ID
     * @param id ID пользователя для удаления
     * @throws EntityNotFoundException если пользователь не найден
     */
    public void deleteUser(Long id) {
        // Проверка существования пользователя по id
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Пользователь не найден: " + id);
        }
        // Удаление пользователя из базы данных
        userRepository.deleteById(id);
    }

    //------------------------------------------------------------------------
    // Методы для работы с балансом пользователя

    /**
     * Списание средств с баланса пользователя
     * @param email email пользователя
     * @param amount сумма для списания
     * @return true если списание успешно, false если недостаточно средств
     * @throws EntityNotFoundException если пользователь не найден
     */
    @Transactional
    public boolean deductBalance(String email, BigDecimal amount) {
        // Поиск пользователя по email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        // Проверка достаточности средств на балансе
        if (user.getBalance().compareTo(amount) >= 0) {
            // Списание суммы с баланса
            user.setBalance(user.getBalance().subtract(amount));
            // Сохранение изменений в базе данных
            userRepository.save(user);
            // Логирование операции списания
            log.info("Списано {} с баланса пользователя {}", amount, email);
            return true; // Успешное списание
        }
        return false; // Недостаточно средств
    }

    /**
     * Пополнение баланса пользователя (внутренний метод)
     * @param email email пользователя
     * @param amount сумма для пополнения
     * @throws EntityNotFoundException если пользователь не найден
     */
    @Transactional
    public void addBalance(String email, BigDecimal amount) {
        // Поиск пользователя по email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        // Добавление суммы к балансу
        user.setBalance(user.getBalance().add(amount));
        // Сохранение изменений в базе данных
        userRepository.save(user);
        // Логирование операции пополнения
        log.info("Добавлено {} на баланс пользователя {}", amount, email);
    }

    /**
     * Получение текущего баланса пользователя
     * @param email email пользователя
     * @return текущий баланс пользователя
     * @throws EntityNotFoundException если пользователь не найден
     */
    @Transactional
    public BigDecimal getBalance(String email) {
        // Поиск пользователя по email и возврат его баланса
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        return user.getBalance();
    }

    /**
     * Публичный метод для пополнения баланса пользователя
     * @param email email пользователя
     * @param amount сумма для пополнения
     * @throws IllegalArgumentException если сумма не положительная
     * @throws EntityNotFoundException если пользователь не найден
     */
    @Transactional
    public void topUpBalance(String email, BigDecimal amount) {
        // Валидация суммы пополнения
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        // Вызов внутреннего метода пополнения баланса
        addBalance(email, amount);
    }
}
//Основная функциональность класса UserService:
//Управление пользователями - создание, поиск и удаление пользователей
//
//Аутентификация - интеграция с Spring Security через UserDetailsService
//
//Управление балансом - пополнение, списание и проверка баланса
//
//Валидация - проверка входных данных и бизнес-логики
//
//Логирование - запись событий в журнал для отладки и мониторинга
//
//Безопасность - хеширование паролей и управление ролями
//
//Класс обеспечивает безопасное управление пользовательскими данными и интеграцию с системой аутентификации Spring Security