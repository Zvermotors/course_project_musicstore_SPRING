package com.example.musicstore.controller;



import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RedirectController {

    @GetMapping("/personal-account")
    public String redirectToPersonalAccount(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin-panel";
        } else {
            return "redirect:/profile";
        }
    }
}