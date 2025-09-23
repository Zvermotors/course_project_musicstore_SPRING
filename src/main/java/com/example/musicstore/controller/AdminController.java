package com.example.musicstore.controller;

import com.example.musicstore.models.Order;
import com.example.musicstore.models.enums.OrderStatus;
import com.example.musicstore.services.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//в этом классе отображается html форма admin-panel
@Controller
@Transactional
public class AdminController {

    @GetMapping("/admin-panel")
    public String adminPanel(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "admin-panel";
    }



}