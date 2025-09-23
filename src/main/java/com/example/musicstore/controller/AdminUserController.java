package com.example.musicstore.controller;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import com.example.musicstore.models.User;
import com.example.musicstore.models.enums.Role;
import com.example.musicstore.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.musicstore.services.UserService;


//Этот класс AdminUserController является контроллером панели
// администратора для управления пользователями.
// Он отвечает за обработку HTTP-запросов, связанных с
// CRUD-операциями над пользователями, и взаимодействует с клиентской частью (веб-страницей).


@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;//содержит бизнес-логику, может использовать UserRepository внутри себя.
    private final UserRepository userRepository;//отвечает за непосредственную работу с базой данных (CRUD-операции).
    private final PasswordEncoder passwordEncoder;




    // Отображение страницы управления пользователями (Read)
    //Метод userManagement() с аннотацией @GetMapping:
    //Загружает всех пользователей из БД через userRepository.findAll().
    //Добавляет в модель все возможные роли (Role.values()) для отображения в форме.
    //Возвращает шаблон admin/users (вероятно, Thymeleaf или FreeMarker), который
    // отрисовывает интерфейс администрирования.
    @GetMapping
    public String userManagement(Model model) {
        model.addAttribute("users", userRepository.findAll());
        // Передаем все значения enum Role вместо запроса к БД
        model.addAttribute("allRoles", Role.values());
        return "admin/users";
    }


//. Обновление ролей пользователей (Update)
//Метод updateUserRoles() с аннотацией @PostMapping("/update-roles"):
//Принимает ID пользователя и список выбранных ролей из формы.
//Гарантирует, что у пользователя всегда есть роль ROLE_USER (базовая роль).
//Обновляет набор ролей пользователя и сохраняет изменения в БД.
//Использует RedirectAttributes для отправки
// сообщений об успехе или ошибке обратно на страницу (после редиректа).
    @PostMapping("/update-roles")
    public String updateUserRoles(
            @RequestParam Long userId,
            @RequestParam(required = false) List<String> roles,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Обрабатываем случай, когда ни одна роль не выбрана
            Set<Role> newRoles = new HashSet<>();
            if (roles != null) {
                newRoles = roles.stream()
                        .map(Role::valueOf)
                        .collect(Collectors.toSet());
            }

            // Гарантируем, что у пользователя всегда есть ROLE_USER
            if (!newRoles.contains(Role.ROLE_USER)) {
                newRoles.add(Role.ROLE_USER);
            }

            user.setRoles(newRoles);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Роли пользователя успешно обновлены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении ролей: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }


    // Создание новых пользователей (Create)
    //Метод createUser() с аннотацией @PostMapping("/create"):
    //Принимает email, пароль и список ролей из формы регистрации.
    //Проверяет, не существует ли уже пользователь с таким email (userRepository.existsByEmail).
    //Хэширует пароль с помощью PasswordEncoder перед сохранением.
    //Преобразует строки с именами ролей в enum-значения Role.
    //Сохраняет нового пользователя в БД.
    @PostMapping("/create")
    public String createUser(@RequestParam String email,
                             @RequestParam String password,
                             @RequestParam List<String> roleNames, // Изменили на String
                             RedirectAttributes redirectAttributes) {
        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Пользователь уже существует");
            return "redirect:/admin/users";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        // Преобразуем имена ролей в enum значения
        Set<Role> roles = roleNames.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        user.setRoles(roles);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Пользователь создан");
        return "redirect:/admin/users";
    }


    //Удаление пользователей (Delete)
    //Метод deleteUser() с аннотацией @PostMapping("/delete/{id}"):
    //Принимает ID пользователя из URL-пути (@PathVariable).
    //Делегирует операцию удаления сервису UserService (который может содержать дополнительную бизнес-логику, например, проверки перед удалением).
    //Возвращает статус операции через flash-атрибуты.
    @PostMapping("/delete/{id}")
    //@PathVariable — это аннотация в Spring Framework,
    // которая позволяет захватывать части URL-адреса и подставлять их в параметры метода контроллера.
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Пользователь успешно удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка удаления: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
