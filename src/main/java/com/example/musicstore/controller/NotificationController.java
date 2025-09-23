package com.example.musicstore.controller;

import com.example.musicstore.models.NotificationRequest;
import com.example.musicstore.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/notifications")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String showNotificationForm(Model model) {
        model.addAttribute("notificationRequest", new NotificationRequest());
        return "admin/notifications";
    }

    @PostMapping("/send")
    public String sendNotification(@ModelAttribute NotificationRequest request, Model model) {
        try {
            notificationService.sendBulkNotification(request);
            model.addAttribute("success", "Уведомления отправлены успешно!");
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при отправке уведомлений: " + e.getMessage());
        }
        return "admin/notifications";
    }

    @GetMapping("/single")
    public String showSingleNotificationForm(Model model) {
        model.addAttribute("email", "");
        model.addAttribute("subject", "");
        model.addAttribute("message", "");
        return "admin/single-notification";
    }

    @PostMapping("/single")
    public String sendSingleNotification(String email, String subject, String message, Model model) {
        log.info("sendSingleNotification: email={}, subject={}, message={}", email, subject, message);
        try {
            notificationService.sendNotificationToUser(email, subject, message);
            model.addAttribute("success", "Уведомление отправлено пользователю: " + email);
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при отправке уведомления: " + e.getMessage());
        }
        return "admin/single-notification";
    }
}
