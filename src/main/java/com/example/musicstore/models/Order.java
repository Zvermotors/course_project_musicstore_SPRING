// Объявление пакета, в котором находится класс
package com.example.musicstore.models;

// Импорт перечисления статусов заказа
import com.example.musicstore.models.enums.OrderStatus;
// Импорт аннотаций JPA для работы с базой данных
import jakarta.persistence.*;
// Импорт аннотаций Lombok для автоматической генерации геттеров, сеттеров и т.д.
import lombok.Data;

// Импорт класса для работы с большими десятичными числами (для денежных сумм)
import java.math.BigDecimal;
// Импорт класса для работы с датой и временем
import java.time.LocalDateTime;

// Аннотация указывает, что этот класс является сущностью JPA
@Entity
// Аннотация задает имя таблицы в базе данных (orders, т.к. order - зарезервированное слово в SQL)
@Table(name = "orders")
// Аннотация Lombok - автоматически генерирует геттеры, сеттеры, toString, equals и hashCode
@Data
public class Order {

    // Аннотация указывает, что это поле является первичным ключом
    @Id
    // Аннотация определяет стратегию генерации идентификатора (автоинкремент в базе данных)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с продуктом (много заказов - один продукт)
    // Аннотация определяет отношение "многие к одному"
    @ManyToOne(fetch = FetchType.EAGER) // Продукт загружается сразу вместе с заказом
    // Аннотация определяет имя столбца внешнего ключа в таблице orders
    @JoinColumn(name = "product_id") // внешний ключ
    private Product product;

    // Связь с пользователем
    // Аннотация определяет отношение "многие к одному"
    @ManyToOne(fetch = FetchType.EAGER) // Пользователь загружается сразу вместе с заказом
    // Аннотация определяет имя столбца внешнего ключа в таблице orders
    @JoinColumn(name = "user_id")
    private User user;

    // Количество товара в заказе
    // Значение по умолчанию - 1
    private Integer quantity = 1;

    // Общая сумма заказа
    // Аннотация определяет имя столбца в таблице
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    // Статус заказа
    // Аннотация указывает, что enum должен сохраняться как строка (а не порядковый номер)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Дата и время создания заказа
    // Аннотация определяет имя столбца в таблице
    @Column(name = "order_date")
    private LocalDateTime orderDate;

    // Дата и время завершения заказа
    // Аннотация определяет имя столбца в таблице
    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    // Метод, выполняемый перед сохранением сущности в базу данных
    // Аннотация JPA - callback метод перед persist операцией
    @PrePersist
    private void init() {
        // Если дата заказа не установлена, устанавливаем текущую дату и время
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        // Если статус не установлен, устанавливаем статус "Ожидание"
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }
}