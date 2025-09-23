// Объявление пакета, в котором находится класс
package com.example.musicstore.models;

// Импорт аннотаций JPA для работы с базой данных
import jakarta.persistence.*;
// Импорт аннотаций Lombok для автоматической генерации кода
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Аннотация указывает, что этот класс является сущностью JPA
@Entity
// Аннотация задает имя таблицы в базе данных
@Table(name = "images")
// Аннотация Lombok - автоматически генерирует геттеры, сеттеры, toString, equals и hashCode
@Data
// Аннотация Lombok - генерирует конструктор со всеми аргументами
@AllArgsConstructor
// Аннотация Lombok - генерирует конструктор без аргументов
@NoArgsConstructor
// Представляет модель изображения для музыкального магазина.
public class Image {

    // Аннотация указывает, что это поле является первичным ключом
    @Id
    // Аннотация определяет стратегию генерации идентификатора (автоматическая)
    @GeneratedValue(strategy = GenerationType.AUTO)
    // Аннотация определяет имя столбца в таблице
    @Column(name = "id")
    private Long id;

    // Название изображения (может быть сгенерированным именем)
    @Column(name = "name")
    private String name;

    // Оригинальное имя файла, которое было у файла при загрузке
    @Column(name = "original_file_name")
    private String originalFileName;

    // Размер файла в байтах
    @Column(name = "size")
    private Long size;

    // MIME-тип содержимого файла (например, "image/jpeg", "image/png")
    @Column(name = "content_type")
    private String contentType;

    // Флаг, указывающий является ли это изображение превью (основным) для товара
    @Column(name = "is_preview_image")
    private boolean isPreviewImage;

    // Содержимое файла изображения в виде массива байтов
    // Аннотация указывает, что это поле должно храниться как Large Object (BLOB)
    @Lob
    private byte[] bytes;

    // Связь с товаром, к которому относится это изображение
    // Аннотация определяет отношение "многие к одному"
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private Product product;
}
// Он сохраняет данные о файле: уникальный идентификатор, название, исходное имя файла, размер, тип контента,
// флаг превью-изображения и содержимое в виде массива байтов. Связан с товаром (`Product`)
// через отношение многие-ко-одному, что позволяет прикреплять изображения к конкретным
// продуктам. Используется для хранения и управления изображениями музыкальных товаров в
// базе данных