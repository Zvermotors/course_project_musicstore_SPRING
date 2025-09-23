package com.example.musicstore.controller;
import com.example.musicstore.models.Product;
import com.example.musicstore.services.ProductService;
import com.example.musicstore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class DopCatalogController {

    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public String catalog(Model model,
                          Principal principal,
                          @RequestParam(required = false) Boolean added) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        if (principal != null) {
            BigDecimal balance = userService.getBalance(principal.getName());
            model.addAttribute("userBalance", balance);
        }

        if (added != null && added) {
            model.addAttribute("success", "Товар добавлен в корзину");
        }

        return "catalog";
    }

    // Быстрая покупка (минуя корзину)
    @PostMapping("/buy-now/{productId}")
    public String buyNow(@PathVariable Long productId,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            Product product = productService.getProductById(productId);
            BigDecimal price = BigDecimal.valueOf(product.getPrice());
            String email = principal.getName();

            // Проверяем баланс
            BigDecimal balance = userService.getBalance(email);
            if (balance.compareTo(price) < 0) {
                redirectAttributes.addFlashAttribute("error", "Недостаточно средств");
                return "redirect:/catalog";
            }

            // Покупаем и списываем средства
            productService.purchaseProduct(productId, email);
            userService.deductBalance(email, price);

            redirectAttributes.addFlashAttribute("success", "Товар успешно куплен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/catalog";
    }

    // Быстрое бронирование
    @PostMapping("/reserve-now/{productId}")
    public String reserveNow(@PathVariable Long productId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            productService.bookProduct(productId, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Товар забронирован!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/catalog";
    }
}