package com.example.musicstore.controller;

import com.example.musicstore.models.Product;
import com.example.musicstore.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    private final ProductService productService;

    @GetMapping
    public String productList(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }

    @GetMapping("/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/productform";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        try {
            productService.saveProduct(product, imageFile);
            redirectAttributes.addFlashAttribute("success", "Товар успешно сохранен");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при сохранении изображения");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/productform";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) throws IOException {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("success", "Товар успешно удален");
        return "redirect:/admin/products";
    }

    // Админ также может бронировать и покупать
    @PostMapping("/{id}/book")
    public String bookProduct(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            productService.bookProduct(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Товар успешно забронирован");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка бронирования: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/purchase")
    public String purchaseProduct(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            productService.purchaseProduct(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Товар успешно куплен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка покупки: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            productService.cancelBooking(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Бронь успешно отменена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка отмены брони: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}