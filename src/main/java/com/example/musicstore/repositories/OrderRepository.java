// Пакет, в котором находится репозиторий
package com.example.musicstore.repositories;

// Импорт модели Order
import com.example.musicstore.models.Order;
// Импорт перечисления статусов заказа
import com.example.musicstore.models.enums.OrderStatus;
// Импорт Spring Data JPA репозитория
import org.springframework.data.jpa.repository.JpaRepository;
// Импорт аннотации для кастомных SQL запросов
import org.springframework.data.jpa.repository.Query;
// Импорт аннотации для параметров запросов
import org.springframework.data.repository.query.Param;
// Импорт аннотации для обозначения репозитория
import org.springframework.stereotype.Repository;

// Импорт класса для точных денежных расчетов
import java.math.BigDecimal;
// Импорт класса для работы с датой и временем
import java.time.LocalDateTime;
// Импорт интерфейса списка
import java.util.List;

/**
 * Репозиторий для работы с заказами (Order entity).
 * Предоставляет методы для CRUD операций и кастомные запросы для работы с заказами.
 */
@Repository // Аннотация указывает, что это компонент репозитория Spring
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Находит все заказы по ID товара
    List<Order> findByProductId(Long productId);

    //----------------------------------------
    // Находит все заказы по ID пользователя
    List<Order> findByUser_Id(Long userId);

    // Находит все заказы по статусу
    List<Order> findByStatus(OrderStatus status);

    // Находит заказы в указанном временном промежутке
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    // Находит заказы по статусу в указанном временном промежутке
    List<Order> findByStatusAndOrderDateBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    // Новые методы для статистики
    // Подсчитывает количество заказов по статусу
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    // Подсчитывает количество заказов по ID товара
    @Query("SELECT COUNT(o) FROM Order o WHERE o.product.id = :productId")
    Long countByProductId(@Param("productId") Long productId);

    // Подсчитывает количество заказов по ID товара и статусу
    @Query("SELECT COUNT(o) FROM Order o WHERE o.product.id = :productId AND o.status = :status")
    Long countByProductIdAndStatus(@Param("productId") Long productId, @Param("status") OrderStatus status);

    // Вычисляет общую выручку за период для завершенных заказов
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :start AND :end AND o.status = 'COMPLETED'")
    BigDecimal getTotalRevenueByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Подсчитывает количество завершенных заказов за период
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :start AND :end AND o.status = 'COMPLETED'")
    Long countCompletedOrdersByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    //--------------------------------------------------------------------------------
    // Находит все заказы, отсортированные по дате (новые сначала)
    List<Order> findAllByOrderByOrderDateDesc();

    // Находит заказы по статусу, отсортированные по дате
    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);

    // Находит заказы по email пользователя или названию товара, отсортированные по дате
    List<Order> findByUserEmailContainingOrProductNameContainingOrderByOrderDateDesc(String email, String productName);

    // Кастомные методы с JOIN FETCH для избежания N+1 проблемы
    // Находит все заказы с загруженными пользователями и товарами
    @Query("SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.product")
    List<Order> findAllWithUserAndProduct();

    // Находит заказы по статусу с загруженными пользователями и товарами
    @Query("SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.product WHERE o.status = :status")
    List<Order> findByStatusWithUserAndProduct(@Param("status") OrderStatus status);

    // Находит заказы пользователя, исключая указанные статусы
    List<Order> findByUserIdAndStatusNotIn(Long userId, List<OrderStatus> cancelled);

    // Находит все заказы пользователя
    List<Order> findByUserId(Long userId);
}
//Что делает интерфейс OrderRepository:
//
//Этот интерфейс является репозиторием Spring Data JPA, который предоставляет:
//
//Наследуемая функциональность:
//
//Все стандартные CRUD операции от JpaRepository
//
//Пагинация и сортировка
//
//Базовые методы работы с сущностью Order
//
//Кастомные методы:
//
//Поисковые методы - поиск заказов по различным критериям (пользователь, товар, статус, дата)
//
//Статистические методы - подсчет заказов, вычисление выручки
//
//Методы с JOIN FETCH - оптимизированные запросы с eager loading связей
//
//Сортировка - методы с сортировкой по дате заказа
//
//Ключевые особенности:
//
//Использует Spring Data JPA для автоматической генерации запросов
//
//Содержит кастомные JPQL запросы для сложной логики
//
//Оптимизирован для избежания N+1 проблемы через JOIN FETCH
//
//Предоставляет методы для аналитики и отчетности
//
//Использование:
//
//Управление заказами в административной панели
//
//Формирование отчетов и статистики
//
//Получение истории заказов пользователей
//
//Интеграция с сервисным слоем приложения