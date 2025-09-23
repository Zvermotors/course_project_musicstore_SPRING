package com.example.musicstore.services;

import com.example.musicstore.models.Order;
import com.example.musicstore.models.Product;
import com.example.musicstore.models.User;
import com.example.musicstore.models.enums.OrderStatus;
import com.example.musicstore.repositories.OrderRepository;
import com.example.musicstore.repositories.ProductRepository;
import com.example.musicstore.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления заказами
 * Обеспечивает бизнес-логику работы с заказами
 */
@Service // Аннотация Spring, указывающая что это сервисный компонент
@RequiredArgsConstructor // Lombok аннотация для генерации конструктора с final полями
@Transactional // Аннотация Spring для управления транзакциями на уровне класса
public class OrderService {

    // Репозиторий для работы с заказами в базе данных
    private final OrderRepository orderRepository;

    // Репозиторий для работы с товарами в базе данных
    private final ProductRepository productRepository;

    // Репозиторий для работы с пользователями в базе данных
    private final UserRepository userRepository;

    // Сервис для работы с товарами
    private final ProductService productService;

    // Логгер для записи событий и ошибок
    private final Logger log = LoggerFactory.getLogger(OrderService.class);

    /**
     * Удаление заказа по идентификатору
     * @param id идентификатор заказа для удаления
     */
    public void deleteOrder(Long id) {
        // 1. Находим заказ по ID или выбрасываем исключение если не найден
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ с ID " + id + " не найден"));

        // 2. Опционально: проверка бизнес-правил (закомментировано)
        // if (order.getStatus() == OrderStatus.COMPLETED) {
        //     throw new IllegalStateException("Нельзя удалить завершенный заказ");
        // }

        // 3. Логируем предупреждение если удаляем подтвержденный заказ
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.warn("Удаление подтвержденного заказа ID: {}", id);
        }

        // 4. Удаляем заказ из репозитория
        orderRepository.delete(order);

        // 5. Логируем успешное удаление заказа
        log.info("Заказ удален: ID {}, Пользователь: {}, Товар: {}",
                id, order.getUser().getEmail(), order.getProduct().getName());
    }

    /**
     * Получение всех заказов
     * @return список всех заказов
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Получение заказа по идентификатору
     * @param id идентификатор заказа
     * @return заказ или null если не найден
     */
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    /**
     * Получение заказов по идентификатору пользователя
     * @param userId идентификатор пользователя
     * @return список заказов пользователя
     */
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUser_Id(userId);
    }

    /**
     * Получение заказов по статусу
     * @param status статус заказа
     * @return список заказов с указанным статусом
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Создание нового заказа
     * @param productId идентификатор товара
     * @param userId идентификатор пользователя
     * @param quantity количество товара
     * @param status статус заказа
     * @return созданный заказ
     */
    @Transactional // Управление транзакцией на уровне метода
    public Order createOrder(Long productId, Long userId, Integer quantity, OrderStatus status) {
        // 1. Поиск товара по ID или исключение если не найден
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 2. Поиск пользователя по ID или исключение если не найден
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Создание нового объекта заказа
        Order order = new Order();

        // 4. Установка свойств заказа
        order.setProduct(product); // Установка товара
        order.setUser(user); // Установка пользователя
        order.setQuantity(quantity); // Установка количества
        // Расчет общей суммы: цена товара * количество
        order.setTotalAmount(BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(quantity)));
        order.setStatus(status); // Установка статуса
        order.setOrderDate(LocalDateTime.now()); // Установка даты заказа

        // 5. Если статус COMPLETED, устанавливаем дату завершения
        if (status == OrderStatus.COMPLETED) {
            order.setCompletedDate(LocalDateTime.now());
        }

        // 6. Сохранение заказа в репозитории
        Order savedOrder = orderRepository.save(order);

        // 7. Синхронизация статуса товара после создания заказа
        productService.syncProductStatusFromOrders(productId);

        // 8. Возврат сохраненного заказа
        return savedOrder;
    }

    /**
     * Обновление статуса заказа
     * @param orderId идентификатор заказа
     * @param newStatus новый статус заказа
     * @return обновленный заказ
     */
    @Transactional // Управление транзакцией на уровне метода
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        // 1. Поиск заказа по ID или исключение если не найден
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Сохранение старого статуса для сравнения
        OrderStatus oldStatus = order.getStatus();

        // 3. Установка нового статуса
        order.setStatus(newStatus);

        // 4. Если новый статус COMPLETED, устанавливаем дату завершения
        if (newStatus == OrderStatus.COMPLETED) {
            order.setCompletedDate(LocalDateTime.now());
        }

        // 5. Сохранение обновленного заказа
        Order savedOrder = orderRepository.save(order);

        // 6. Автоматическая синхронизация статуса продукта при изменении статуса
        if (oldStatus != newStatus) {
            productService.syncProductStatusFromOrders(order.getProduct().getId());
        }

        // 7. Возврат обновленного заказа
        return savedOrder;
    }

    /**
     * Получение общего количества заказов
     * @return общее количество заказов
     */
    public Long getTotalOrdersCount() {
        return orderRepository.count();
    }

    /**
     * Получение количества заказов по статусу
     * @param status статус заказа
     * @return количество заказов с указанным статусом
     */
    public Long getOrdersCountByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Получение общей выручки
     * @return общая выручка от всех заказов
     */
    public BigDecimal getTotalRevenue() {
        // Получение выручки за весь период (с очень ранней даты до текущего момента)
        return orderRepository.getTotalRevenueByPeriod(
                LocalDateTime.of(2000, 1, 1, 0, 0), // Начальная дата
                LocalDateTime.now() // Конечная дата (текущее время)
        );
    }

    /**
     * Получение всех заказов отсортированных по дате (новые сначала)
     * @return отсортированный список заказов
     */
    public List<Order> getAllOrdersSorted() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    /**
     * Получение заказов по статусу отсортированных по дате
     * @param status статус заказа
     * @return отсортированный список заказов с указанным статусом
     */
    public List<Order> getOrdersByStatusSorted(OrderStatus status) {
        return orderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    /**
     * Поиск заказов по email пользователя или названию товара
     * @param search строка поиска
     * @return список найденных заказов отсортированных по дате
     */
    public List<Order> searchOrders(String search) {
        return orderRepository.findByUserEmailContainingOrProductNameContainingOrderByOrderDateDesc(search, search);
    }

    /**
     * Получить все заказы пользователя
     * @param userId ID пользователя
     * @return список заказов пользователя
     */
    public List<Order> getUserOrders(Long userId) {
        try {
            // Логирование начала операции
            log.debug("Getting orders for user ID: {}", userId);

            // Поиск заказов пользователя
            List<Order> orders = orderRepository.findByUserId(userId);

            // Логирование успешного завершения
            log.info("Found {} orders for user ID: {}", orders.size(), userId);

            return orders;
        } catch (Exception e) {
            // Логирование ошибки
            log.error("Error getting orders for user ID: {}", userId, e);

            // Выброс исключения с понятным сообщением
            throw new RuntimeException("Не удалось получить заказы пользователя", e);
        }
    }

    /**
     * Получить активные заказы пользователя (не отмененные и не завершенные)
     * @param userId ID пользователя
     * @return список активных заказов
     */
    public List<Order> getUserActiveOrders(Long userId) {
        try {
            // Логирование начала операции
            log.debug("Getting active orders for user ID: {}", userId);

            // Поиск активных заказов (исключая отмененные и завершенные)
            List<Order> activeOrders = orderRepository.findByUserIdAndStatusNotIn(
                    userId,
                    List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED)
            );

            // Логирование успешного завершения
            log.info("Found {} active orders for user ID: {}", activeOrders.size(), userId);

            return activeOrders;
        } catch (Exception e) {
            // Логирование ошибки
            log.error("Error getting active orders for user ID: {}", userId, e);

            // Выброс исключения с понятным сообщением
            throw new RuntimeException("Не удалось получить активные заказы", e);
        }
    }
}