package com.example.musicstore.controller;

import com.example.musicstore.models.User;
import com.example.musicstore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


//это контроллер Spring MVC, отвечающий за управление пользовательскими
// страницами и действиями связанные с регистрацией и входом в систему.

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/login")
    public String login() {
        return "login";
    }


    //отображение страницы регистрации
    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    //регистрация пользователя
    @PostMapping("/registration")
    public String createUser(@ModelAttribute("user") User user,//Spring автоматически связывает
                             // поля формы с объектом User, объект формы "user" находится в html форме registration
                             BindingResult result,
                             Model model) {

        log.info("Попытка регистрации по электронной почте: {}", user.getEmail());

        // Валидация обязательных полей
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            result.rejectValue("email", "error.email", "Email обязателен");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            result.rejectValue("password", "error.password", "Пароль обязателен");
        }

        if (result.hasErrors()) {
            log.warn("Validation errors: {}", result.getAllErrors());
            return "registration";
        }


        //Создание пользователя через метод createUser
        try {
            if (!userService.createUser(user)) {
                model.addAttribute("errorMessage", "Пользователь с email: " + user.getEmail() + " уже существует");
                return "registration";
            }
            return "redirect:/login?success";
        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("errorMessage", "Ошибка регистрации: " + e.getMessage());
            return "registration";
        }
    }
}