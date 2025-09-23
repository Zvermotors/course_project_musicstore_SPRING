package com.example.musicstore.controller;

import com.example.musicstore.models.Product;
import com.example.musicstore.repositories.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;



import java.util.List;


@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final ProductService productService;

    //метод для обработки изображения при нажатии "Подробнее"
    @GetMapping("/product/{id}")
    public String showProductDetails(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product-details"; // имя шаблона для страницы товара
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        // Контроллер автоматически добавит аутентификацию через SecurityContext
        return "about";
    }


}