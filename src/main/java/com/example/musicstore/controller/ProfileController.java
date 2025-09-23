package com.example.musicstore.controller;

import com.example.musicstore.models.UserProfile;
import com.example.musicstore.services.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для управления профилем пользователя
 * Обрабатывает операции просмотра обновления профиля пользователя
 */
@Controller // Аннотация Spring, указывающая что это контроллер MVC
@RequiredArgsConstructor // Lombok аннотация для генерации конструктора с final полями
public class ProfileController {

    // Сервис для работы с профилями пользователей
    private final ProfileService profileService;

    /**
     * Отображение страницы профиля пользователя
     * @param model объект Model для передачи данных в представление
     * @param authentication объект аутентификации Spring Security
     * @return имя шаблона страницы профиля
     */
    @GetMapping("/profile") // Обработка GET запросов по пути /profile
    public String profile(Model model, Authentication authentication) {
        // Получаем email аутентифицированного пользователя из контекста безопасности
        String email = authentication.getName();

        try {
            // 1. Пытаемся загрузить профиль из базы данных по email пользователя
            UserProfile user = profileService.getCurrentUserProfile(email);
            // Добавляем найденный профиль в модель для заполнения формы
            model.addAttribute("userForm", user); // Форма заполнится существующими данными
        } catch (Exception e) {
            // 2. Если произошла ошибка (профиля нет или проблемы с БД)
            // Создаем пустой объект профиля для формы
            model.addAttribute("userForm", new UserProfile()); // Пустая форма
            // Добавляем сообщение об ошибке в модель
            model.addAttribute("error", "Не удалось загрузить данные профиля"); // Сообщение об ошибке
        }

        // Возвращаем имя шаблона Thymeleaf
        return "profile";
    }

    /**
     * Обработка обновления профиля пользователя
     * @param userForm объект профиля из формы с валидацией
     * @param bindingResult объект для проверки ошибок валидации
     * @param authentication объект аутентификации Spring Security
     * @param redirectAttributes атрибуты для перенаправления с flash-сообщениями
     * @return перенаправление на страницу профиля или возврат формы с ошибками
     */
    @PostMapping("/profile/update") // Обработка POST запросов по пути /profile/update
    public String handleProfileUpdate(@ModelAttribute("userForm") @Valid UserProfile userForm, // Привязка данных формы с валидацией
                                      BindingResult bindingResult, // Объект для результатов валидации
                                      Authentication authentication, // Объект аутентификации
                                      RedirectAttributes redirectAttributes) { // Атрибуты для flash-сообщений

        // Проверка наличия ошибок валидации
        if (bindingResult.hasErrors()) {
            // Если есть ошибки валидации, возвращаемся на форму профиля
            return "profile";
        }

        // Защита от подмены email в форме:
        // Берет email из данных аутентификации (authentication.getName())
        // Перезаписывает email из формы на email аутентифицированного пользователя
        String currentEmail = authentication.getName();
        userForm.setEmail(currentEmail); // Фиксируем email из аутентификации

        try {
            // Получаем email текущего аутентифицированного пользователя
            String currentUserEmail = authentication.getName();

            // Проверяем, существует ли уже профиль пользователя в БД
            if (profileService.profileExists(currentUserEmail)) {
                // Если ДА → updateProfile() - обновляем существующий профиль
                profileService.updateProfile(userForm, currentUserEmail);
                // Добавляем flash-сообщение об успешном обновлении
                redirectAttributes.addFlashAttribute("message", "Профиль успешно обновлен");
            } else {
                // Если НЕТ → createNewUserProfile() - создаем новый профиль
                profileService.createNewUserProfile(userForm);
                // Добавляем flash-сообщение об успешном создании
                redirectAttributes.addFlashAttribute("message", "Профиль успешно создан");
            }

        } catch (Exception e) {
            // Обработка исключений - добавление сообщения об ошибке
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            // Возврат на страницу профиля при ошибке
            return "profile";
        }

        // Перенаправление на страницу профиля после успешной операции
        return "redirect:/profile";
    }
}