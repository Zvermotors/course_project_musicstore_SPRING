// Объявление пакета, в котором находится тестовый класс
package com.example.musicstore.services;

// Импорт аннотации Test из JUnit для обозначения тестовых методов
import org.junit.jupiter.api.Test;
// Импорт аннотации ExtendWith для расширения функциональности тестов
import org.junit.jupiter.api.extension.ExtendWith;
// Импорт аннотации InjectMocks для внедрения mock-объектов
import org.mockito.InjectMocks;
// Импорт аннотации Mock для создания mock-объектов
import org.mockito.Mock;
// Импорт расширения Mockito для JUnit 5
import org.mockito.junit.jupiter.MockitoExtension;
// Импорт интерфейса JavaMailSender для отправки email
import org.springframework.mail.javamail.JavaMailSender;
// Импорт интерфейса TemplateEngine для обработки шаблонов
import org.thymeleaf.TemplateEngine;
// Импорт класса Context для передачи данных в шаблоны
import org.thymeleaf.context.Context;

// Импорт класса MimeMessage для работы с email сообщениями
import jakarta.mail.internet.MimeMessage;

// Импорт статических методов для работы с Mockito
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Аннотация для интеграции Mockito с JUnit 5
@ExtendWith(MockitoExtension.class)
// Объявление тестового класса для EmailService
class EmailServiceTest {

    // Создание mock-объекта для отправки email
    @Mock
    private JavaMailSender mailSender;

    // Создание mock-объекта для обработки шаблонов
    @Mock
    private TemplateEngine templateEngine;

    // Создание mock-объекта для email сообщения
    @Mock
    private MimeMessage mimeMessage;

    // Внедрение mock-объектов в тестируемый сервис email
    @InjectMocks
    private EmailService emailService;

    // Тест для отправки приветственного email
    @Test
    void sendWelcomeEmail_ShouldProcessTemplateAndSendEmail() {
        // Arrange (подготовка) - настройка поведения mock-объектов
        // Когда вызывается createMimeMessage(), возвращать mock-объект mimeMessage
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // Когда вызывается process() с любыми строковыми аргументами и контекстом, возвращать HTML-контент
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Welcome</html>");

        // Act (действие) - вызов тестируемого метода
        // Отправка приветственного email на указанный адрес с именем пользователя
        emailService.sendWelcomeEmail("test@example.com", "Test User");

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что метод process() был вызван для обработки шаблона
        verify(templateEngine).process(anyString(), any(Context.class));
        // Проверка, что метод send() был вызван для отправки email
        verify(mailSender).send(any(MimeMessage.class));
    }

    // Тест для отправки уведомительного email
    @Test
    void sendNotificationEmail_ShouldProcessTemplateWithCorrectContext() {
        // Arrange (подготовка) - настройка поведения mock-объектов
        // Когда вызывается createMimeMessage(), возвращать mock-объект mimeMessage
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // Когда вызывается process() с любыми строковыми аргументами и контекстом, возвращать HTML-контент
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Notification</html>");

        // Act (действие) - вызов тестируемого метода
        // Отправка уведомительного email с указанными параметрами
        emailService.sendNotificationEmail(
                "test@example.com",     // email адрес получателя
                "Test User",            // имя пользователя
                "Test Subject",         // тема письма
                "Test Message"          // текст сообщения
        );

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что метод process() был вызван для обработки шаблона
        verify(templateEngine).process(anyString(), any(Context.class));
        // Проверка, что метод send() был вызван для отправки email
        verify(mailSender).send(any(MimeMessage.class));
    }
}