package com.example.musicstore.controller;

import com.example.musicstore.models.User;
import com.example.musicstore.services.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;



//Этот класс UserManagementController является контроллером для управления
// статусом пользователей в админ-панели.
// Он предоставляет функционал для просмотра и изменения активности пользователей.
@Slf4j
@Controller
@RequestMapping("/admin/user-management")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;


    // Отображение списка пользователей со статистикой
    //Метод displayUsers() с аннотацией @GetMapping:
    //Получает полный список пользователей через сервис userManagementService.findAllUsers()
    //Вычисляет статистику:
    //activeUsersCount - количество активных пользователей
    //inactiveUsersCount - количество неактивных пользователей
    //usersWithBalanceCount - количество пользователей с положительным балансом
    //Добавляет данные в модель для отображения в шаблоне admin/user-management
    //Логирует информацию для отладки (видно по @Slf4j и System.out.println)
    @GetMapping
    public String displayUsers(Model model) {

            log.info("🎯 Контроллер вызван! 🎯");
            System.out.println("=== КОНТРОЛЛЕР РАБОТАЕТ ===");
        List<User> userList = userManagementService.findAllUsers();
             log.info("Найдено пользователей: {}", userList.size());
        // Подсчет статистики
        long activeUsersCount = userList.stream().filter(User::isActive).count();
        long inactiveUsersCount = userList.size() - activeUsersCount;
        long usersWithBalanceCount = userList.stream()
                .filter(user -> user.getBalance() != null && user.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .count();

        model.addAttribute("userList", userList);
        model.addAttribute("activeUsersCount", activeUsersCount);
        model.addAttribute("inactiveUsersCount", inactiveUsersCount);
        model.addAttribute("usersWithBalanceCount", usersWithBalanceCount);

        return "admin/user-management";
    }

    //Переключение статуса пользователя (активный/неактивный)
    //Метод toggleUserStatus() с аннотацией @PostMapping("/toggle-status"):
    //Принимает ID пользователя и новый статус из формы
    //Вызывает сервис userManagementService.updateUserActiveStatus() для обновления статуса
    //Выполняет перенаправление обратно на страницу управления пользователями
    @PostMapping("/toggle-status")
    public String toggleUserStatus(@RequestParam Long userId,
                                   @RequestParam boolean isActive) {
        userManagementService.updateUserActiveStatus(userId, isActive);
        return "redirect:/admin/user-management";
    }
}