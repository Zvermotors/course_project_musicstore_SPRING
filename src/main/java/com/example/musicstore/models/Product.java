// Объявление пакета, в котором находится класс
package com.example.musicstore.models;

// Импорт аннотаций JPA для работы с базой данных
import jakarta.persistence.*;
// Импорт аннотаций валидации для проверки данных
import jakarta.validation.constraints.*;
// Импорт аннотаций Lombok для автоматической генерации кода
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Импорт класса для работы с датой и временем
import java.time.LocalDateTime;
// Импорт класса для работы со списками
import java.util.ArrayList;
import java.util.List;

// Аннотация указывает, что этот класс является сущностью JPA
@Entity
// Аннотация задает имя таблицы в базе данных
@Table(name = "products")
// Аннотация Lombok - автоматически генерирует геттеры, сеттеры, toString, equals и hashCode
@Data
// Аннотация Lombok - генерирует конструктор без аргументов
@NoArgsConstructor
// Аннотация Lombok - генерирует конструктор со всеми аргументами
@AllArgsConstructor
public class Product {

    // Аннотация указывает, что это поле является первичным ключом
    @Id
    // Аннотация определяет стратегию генерации идентификатора (автоинкремент в базе данных)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Название товара
    // Аннотация указывает, что поле не может быть null в базе данных
    @Column(nullable = false)
    private String name;

    // Описание товара
    // Аннотация определяет тип столбца как TEXT (для длинных текстов)
    @Column(columnDefinition = "TEXT")
    private String description;

    // Цена товара
    // Аннотация указывает, что поле не может быть null в базе данных
    @Column(nullable = false)
    private Double price;

    // Автор/продавец товара (email пользователя)
    // Аннотация указывает, что поле не может быть null в базе данных
    @Column(nullable = false)
    private String author;

    // Путь к изображению товара
    @Column(name = "image_path")
    private String imagePath;

    // Статус товара
    // Аннотация указывает, что enum должен сохраняться как строка
    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.AVAILABLE; // AVAILABLE, BOOKED, SOLD

    // Пользователь, который забронировал товар
    // Аннотация определяет отношение "многие к одному"
    @ManyToOne
    // Аннотация определяет имя столбца внешнего ключа
    @JoinColumn(name = "booked_by_id")
    private User bookedBy;

    // Пользователь, который купил товар
    // Аннотация определяет отношение "многие к одному"
    @ManyToOne
    // Аннотация определяет имя столбца внешнего ключа
    @JoinColumn(name = "buyer_id")
    private User buyer;

    // Список заказов для этого товара
    // Аннотация определяет отношение "один ко многим"
    // mappedBy указывает поле в сущности Order, которое владеет связью
    // cascade = ALL - операции сохраняются для связанных заказов
    // fetch = LAZY - заказы загружаются только при явном обращении
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    // Срок действия бронирования товара
    private LocalDateTime bookingExpiry;

    // Вложенное перечисление статусов товара
    public enum ProductStatus {
        AVAILABLE,  // Товар доступен для покупки
        BOOKED,     // Товар забронирован (в корзине у пользователя)
        SOLD        // Товар продан
    }
}