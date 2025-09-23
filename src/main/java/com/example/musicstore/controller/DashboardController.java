package com.example.musicstore.controller;

import com.example.musicstore.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

//Этот класс DashboardController является контроллером панели управления (дашборда) администратора. Он отвечает
// за отображение сводной статистики и информации на главной странице админ-панели.
//Администратор входит в систему и переходит по ссылке /admin/dashboard.
//Spring Security проверяет, имеет ли пользователь роль ADMIN.
//Если да — вызывается метод dashboard() контроллера.
//Контроллер запрашивает актуальную статистику у ReportService.
//Статистика передается в шаблонизатор (Thymeleaf или FreeMarker).
//Пользователь видит красивый дашборд с цифрами, графиками или другой сводной информацией.
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ReportService reportService;

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String dashboard(Model model) {
        //Контроллер запрашивает актуальную статистику у ReportService.
        model.addAttribute("stats", reportService.getDashboardStats());
        return "admin/dashboard";
    }
}