package com.example.musicstore.controller;

import com.example.musicstore.models.Order;
import com.example.musicstore.models.Product;
import com.example.musicstore.models.User;
import com.example.musicstore.services.OrderService;
import com.example.musicstore.services.ProductService;
import com.example.musicstore.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;



//контроллер отображающий главную
// страницу и заказы аутентифицированного пользователя



@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;



    @GetMapping("/home")
    public String home(Model model,
                       @RequestParam(required = false) String title,
                       Authentication authentication,
                       HttpServletRequest request) {

        try {
            // Поиск товаров
            List<Product> products;
            if (title != null && !title.trim().isEmpty()) {
                products = productService.findByTitleContaining(title);
                model.addAttribute("searchTerm", title.trim());
            } else {
                products = productService.getAllProducts();
            }
            model.addAttribute("products", products);

            // Заказы пользователя (если авторизован)
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                User user = userService.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

                // Получаем все заказы пользователя
                List<Order> userOrders = orderService.getUserOrders(user.getId());
                model.addAttribute("userOrders", userOrders);

                // Или только активные заказы:
                // List<Order> activeOrders = orderService.getUserActiveOrders(user.getId());
                // model.addAttribute("userOrders", activeOrders);
            }

        } catch (Exception e) {
            log.error("Error in home controller", e);
            model.addAttribute("error", "Произошла ошибка при загрузке данных");
        }

        return "home";
    }



}