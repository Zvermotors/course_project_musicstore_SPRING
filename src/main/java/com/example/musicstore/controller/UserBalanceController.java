package com.example.musicstore.controller;

import com.example.musicstore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/balance")
@RequiredArgsConstructor
public class UserBalanceController {

    private final UserService userService;

    @PostMapping("/top-up")
    public String topUpBalance(@RequestParam BigDecimal amount,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            userService.topUpBalance(principal.getName(), amount);
            redirectAttributes.addFlashAttribute("success", "Баланс пополнен на " + amount + " ₽");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/cart";
    }
}