package com.example.musicstore.controller;

import com.example.musicstore.models.Order;
import com.example.musicstore.models.enums.OrderStatus;
import com.example.musicstore.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

/**
 * Контроллер для управления заказами в административной панели
 * Обрабатывает HTTP-запросы связанные с просмотром, обновлением и удалением заказов
 * Требует прав администратора для доступа к функционалу
 */
@Controller // Аннотация Spring, указывающая что это контроллер MVC
@RequestMapping("/admin/orders") // Базовый путь для всех методов контроллера
@RequiredArgsConstructor // Lombok аннотация для генерации конструктора с final полями
public class OrderController {

    @Autowired // Внедрение зависимости сервиса заказов
    private OrderService orderService;

    /**
     * Отображение списка заказов с возможностью фильтрации и поиска
     * @param model объект Model для передачи данных в представление
     * @param status параметр запроса для фильтрации по статусу (опциональный)
     * @param search параметр запроса для поиска (опциональный)
     * @return имя шаблона для отображения
     */
    @GetMapping // Обработка GET запросов по пути /admin/orders
    public String viewOrders(Model model,
                             @RequestParam(required = false) String status, // Опциональный параметр статуса
                             @RequestParam(required = false) String search) { // Опциональный параметр поиска
        try {
            List<Order> orders; // Список для хранения заказов

            // Фильтрация по статусу если параметр передан и не пустой
            if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
                try {
                    // Преобразование строки в enum OrderStatus
                    OrderStatus orderStatus = OrderStatus.valueOf(status);
                    // Получение заказов по статусу
                    orders = orderService.getOrdersByStatus(orderStatus);
                } catch (IllegalArgumentException e) {
                    // В случае неверного статуса - получаем все заказы
                    orders = orderService.getAllOrders();
                }
            } else {
                // Если статус не указан или "ALL" - получаем все заказы
                orders = orderService.getAllOrders();
            }

            // Поиск заказов если параметр поиска передан и не пустой
            if (search != null && !search.trim().isEmpty()) {
                orders = orderService.searchOrders(search);
            }

            // Сортировка заказов по дате (новые сначала)
            orders.sort(Comparator.comparing(Order::getOrderDate).reversed());

            // Добавление данных в модель для передачи в представление
            model.addAttribute("orders", orders); // Список заказов
            model.addAttribute("orderStatuses", OrderStatus.values()); // Все возможные статусы
            model.addAttribute("currentStatus", status); // Текущий выбранный статус для сохранения фильтра

            return "admin/orders"; // Имя шаблона Thymeleaf

        } catch (Exception e) {
            // Обработка ошибок - добавление сообщения об ошибке в модель
            model.addAttribute("error", "Ошибка при загрузке заказов: " + e.getMessage());
            return "admin/orders"; // Возврат того же шаблона с сообщением об ошибке
        }
    }

    /**
     * Просмотр деталей конкретного заказа
     * @param id идентификатор заказа
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона для отображения деталей заказа
     */
    @GetMapping("/{id}") // Обработка GET запросов по пути /admin/orders/{id}
    @PreAuthorize("hasAuthority('ADMIN')") // Проверка прав доступа - только для ADMIN
    public String orderDetails(@PathVariable Long id, Model model) { // Извлечение ID из пути
        // Получение заказа по ID
        Order order = orderService.getOrderById(id);
        // Добавление заказа в модель
        model.addAttribute("order", order);
        return "admin/order-details"; // Имя шаблона деталей заказа
    }

    /**
     * Обновление статуса заказа
     * @param id идентификатор заказа
     * @param status новый статус заказа
     * @param model объект Model для передачи данных в представление
     * @return перенаправление на страницу списка заказов
     */
    @PostMapping("/{id}/update-status") // Обработка POST запросов по пути /admin/orders/{id}/update-status
    @PreAuthorize("hasAuthority('ADMIN')") // Проверка прав доступа - только для ADMIN
    public String updateOrderStatus(@PathVariable Long id, // Извлечение ID из пути
                                    @RequestParam OrderStatus status, // Параметр нового статуса
                                    Model model) {
        try {
            // Вызов сервиса для обновления статуса заказа
            orderService.updateOrderStatus(id, status);
            // Добавление сообщения об успехе в модель
            model.addAttribute("success", "Статус заказа обновлен");
        } catch (Exception e) {
            // Добавление сообщения об ошибке в модель
            model.addAttribute("error", "Ошибка обновления статуса: " + e.getMessage());
        }

        // Перенаправление обратно на страницу списка заказов
        return "redirect:/admin/orders";
    }

    /**
     * Удаление заказа
     * @param id идентификатор заказа для удаления
     * @param redirectAttributes атрибуты для перенаправления (flash-сообщения)
     * @return перенаправление на страницу списка заказов
     */
    @PostMapping("/{id}/delete") // Обработка POST запросов по пути /admin/orders/{id}/delete
    public String deleteOrder(@PathVariable Long id, // Извлечение ID из пути
                              RedirectAttributes redirectAttributes) { // Атрибуты для flash-сообщений
        try {
            // Вызов сервиса для удаления заказа
            orderService.deleteOrder(id);
            // Добавление flash-сообщения об успехе
            redirectAttributes.addFlashAttribute("success", "Заказ успешно удален");
        } catch (Exception e) {
            // Добавление flash-сообщения об ошибке
            redirectAttributes.addFlashAttribute("error", "Ошибка удаления: " + e.getMessage());
        }
        // Перенаправление на страницу списка заказов
        return "redirect:/admin/orders";
    }
}