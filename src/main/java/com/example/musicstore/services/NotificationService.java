package com.example.musicstore.services;

import com.example.musicstore.models.NotificationRequest;
import com.example.musicstore.models.User;
import com.example.musicstore.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для отправки уведомлений пользователям
 * Обеспечивает массовую и индивидуальную рассылку уведомлений
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Основной метод для массовой рассылки уведомлений
     * Асинхронно отправляет уведомления группе пользователей
     * @param request объект запроса с параметрами рассылки
     */
    @Async
    public void sendBulkNotification(NotificationRequest request) {
        List<User> users = getUsersByType(request);
        log.info("Sending notifications to {} users", users.size());

        for (User user : users) {
            if (user.isActive()) {
                emailService.sendNotificationEmail(
                        user.getEmail(),
                        "HELLO",
                        request.getSubject(),
                        request.getMessage()
                );
            }
        }
    }

    /**
     * Вспомогательный метод для получения пользователей по типу рассылки
     * @param request объект запроса с параметрами рассылки
     * @return список пользователей для отправки уведомлений
     */
    private List<User> getUsersByType(NotificationRequest request) {
        switch (request.getType()) {
            case ALL_USERS:
                return userRepository.findAll();
            case ACTIVE_USERS:
                return userRepository.findByActiveTrue();
            case SPECIFIC_USER:
                if (request.getTargetEmail() != null) {
                    return userRepository.findByEmail(request.getTargetEmail())
                            .map(List::of)
                            .orElse(List.of());
                }
                return List.of();
            default:
                return List.of();
        }
    }

    /**
     * Отправка приветственного письма новому пользователю
     * Выполняется асинхронно
     * @param user объект пользователя
     */
    @Async
    public void sendWelcomeEmail(User user) {
        emailService.sendWelcomeEmail(user.getEmail(), user.getEmail());
    }

    /**
     * Отправка уведомления конкретному пользователю по email
     * @param email адрес электронной почты пользователя
     * @param subject тема уведомления
     * @param message текст уведомления
     */
    public void sendNotificationToUser(String email, String subject, String message) {
        userRepository.findByEmail(email).ifPresent(user -> {
            emailService.sendNotificationEmail(
                    user.getEmail(),
                    user.getEmail(),
                    subject,
                    message
            );
        });
    }

    /**
     * Отправка письма для восстановления пароля
     * Выполняется асинхронно
     * @param email адрес электронной почты пользователя
     * @param resetToken токен для восстановления пароля
     */
    @Async
    public void sendPasswordResetEmail(String email, String resetToken) {
        userRepository.findByEmail(email).ifPresent(user -> {
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getEmail(),
                    resetToken
            );
        });
    }
}