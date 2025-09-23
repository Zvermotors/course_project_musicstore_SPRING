package com.example.musicstore.controller;

import com.example.musicstore.models.Cart;
import com.example.musicstore.models.CartItem;
import com.example.musicstore.models.Product;
import com.example.musicstore.services.ProductService;
import com.example.musicstore.services.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ProductService productService;
    private final UserService userService;

    // Добавление в корзину
    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            HttpSession session,
                            Principal principal) {
        Cart cart = getOrCreateCart(session);
        Product product = productService.getProductById(productId);

        cart.addItem(product);
        session.setAttribute("cart", cart);

        return "redirect:/catalog?added=true";
    }

    // Просмотр корзины
    @GetMapping
    public String viewCart(Model model,
                           HttpSession session,
                           Principal principal) {
        Cart cart = getOrCreateCart(session);
        model.addAttribute("cart", cart);

        if (principal != null) {
            BigDecimal balance = userService.getBalance(principal.getName());
            model.addAttribute("userBalance", balance);
        }

        return "cart";
    }

    // Удаление из корзины
    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId,
                                 HttpSession session) {
        Cart cart = getOrCreateCart(session);
        cart.removeItem(productId);
        session.setAttribute("cart", cart);

        return "redirect:/cart";
    }

    // Обновление количества
    @PostMapping("/update/{productId}")
    public String updateQuantity(@PathVariable Long productId,
                                 @RequestParam int quantity,
                                 HttpSession session) {
        Cart cart = getOrCreateCart(session);
        cart.updateQuantity(productId, quantity);
        session.setAttribute("cart", cart);

        return "redirect:/cart";
    }

    // Оформление заказа (покупка)
    @PostMapping("/checkout/buy")
    public String checkoutBuy(HttpSession session,
                              Principal principal,
                              Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        Cart cart = getOrCreateCart(session);
        String email = principal.getName();

        // Проверяем достаточно ли средств
        BigDecimal balance = userService.getBalance(email);
        if (balance.compareTo(cart.getTotalAmount()) < 0) {
            model.addAttribute("error", "Недостаточно средств на счете");
            model.addAttribute("cart", cart);
            model.addAttribute("userBalance", balance);
            return "cart";
        }

        // Покупаем каждый товар в корзине
        for (CartItem item : cart.getItems().values()) {
            try {
                productService.purchaseProduct(item.getProductId(), email);
            } catch (Exception e) {
                model.addAttribute("error", "Ошибка при покупке товара: " + e.getMessage());
                model.addAttribute("cart", cart);
                model.addAttribute("userBalance", balance);
                return "cart";
            }
        }

        // Списание средств
        userService.deductBalance(email, cart.getTotalAmount());

        // Очищаем корзину
        cart.clear();
        session.setAttribute("cart", cart);

        return "redirect:/cart?success=buy";
    }

    // Оформление бронирования
    @PostMapping("/checkout/reserve")
    public String checkoutReserve(HttpSession session,
                                  Principal principal,
                                  Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        Cart cart = getOrCreateCart(session);
        String email = principal.getName();

        // Бронируем каждый товар в корзине
        for (CartItem item : cart.getItems().values()) {
            try {
                productService.bookProduct(item.getProductId(), email);
            } catch (Exception e) {
                model.addAttribute("error", "Ошибка при бронировании товара: " + e.getMessage());
                model.addAttribute("cart", cart);
                model.addAttribute("userBalance", userService.getBalance(email));
                return "cart";
            }
        }

        // Очищаем корзину
        cart.clear();
        session.setAttribute("cart", cart);

        return "redirect:/cart?success=reserve";
    }

    private Cart getOrCreateCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }
}