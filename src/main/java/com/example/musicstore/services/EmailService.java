package com.example.musicstore.services;

import com.example.musicstore.models.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Сервис для отправки электронных писем
 * Предоставляет функциональность для отправки различных типов email-уведомлений
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Отправка простого текстового email-сообщения
     * @param to адрес получателя
     * @param subject тема письма
     * @param text текст сообщения
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    /**
     * Внутренний метод для отправки HTML-писем
     * Использует Thymeleaf шаблоны для формирования HTML-контента
     * @param to адрес получателя
     * @param subject тема письма
     * @param templateName имя шаблона Thymeleaf
     * @param context контекст с переменными для шаблона
     * @throws RuntimeException если отправка письма не удалась
     */
    private void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Этот вариант работает без настройки алиаса
            helper.setFrom("madam.tanyh@yandex.ru", "Music Store");
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    /**
     * Отправка приветственного письма новому пользователю
     * @param to адрес получателя
     * @param userName имя пользователя
     */
    public void sendWelcomeEmail(String to, String userName) {
        Context context = new Context();
        context.setVariable("userName", userName);
        sendHtmlEmail(to, "Добро пожаловать в MusicStore!", "emails/welcome-email", context);
    }

    /**
     * Отправка уведомительного письма с детальной информацией
     * Включает логирование деталей отправки для отладки
     * @param to адрес получателя
     * @param userName имя пользователя
     * @param subject тема письма
     * @param message текст уведомления
     */
    public void sendNotificationEmail(String to, String userName, String subject, String message) {
        log.info("=== EMAIL SENDING DETAILS ===");
        log.info("Recipient (to): {}", to);
        log.info("User Name: {}", userName);
        log.info("Subject: {}", subject);
        log.info("Message length: {} characters", message.length());
        log.info("=============================");

        // Для отладки можно также вывести начало сообщения
        if (log.isDebugEnabled()) {
            String preview = message.length() > 100 ? message.substring(0, 100) + "..." : message;
            log.debug("Message preview: {}", preview);
        }

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("message", message);
        context.setVariable("subject", subject);
        sendHtmlEmail(to, subject, "emails/notification-email", context);

        log.info("Email sent successfully to: {}", to);
    }

    /**
     * Отправка письма для восстановления пароля
     * Содержит токен для сброса пароля пользователя
     * @param to адрес получателя
     * @param userName имя пользователя
     * @param resetToken токен для восстановления пароля
     */
    public void sendPasswordResetEmail(String to, String userName, String resetToken) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("resetToken", resetToken);
        sendHtmlEmail(to, "Восстановление пароля - MusicStore", "emails/password-reset-email", context);
    }
}