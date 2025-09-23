package com.example.musicstore.repositories;

import com.example.musicstore.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
   //Метод поиска по статусу (автогенерация)
    List<Product> findByStatus(Product.ProductStatus status);

    //Метод с двумя условиями (автогенерация)
    List<Product> findAllByStatusAndBookingExpiryBefore(
            Product.ProductStatus status,
            LocalDateTime expiryDate
    );

    //Метод с кастомным JPQL-запросом (уникальные авторы)
    //Что делает
    //Находит уникальных авторов товаров
    //Исключает null значения
    //Сортирует по алфавиту
    //Возвращает только имена авторов (не полные объекты)
    @Query("SELECT DISTINCT p.author FROM Product p WHERE p.author IS NOT NULL ORDER BY p.author")
    List<String> findAllDistinctAuthors();


    //Сложный JPQL-запрос для статистики статусов
    //Что делает:
    //Считает товары по каждому статусу в одном запросе
    //Создает объект StatusDistribution с тремя полями:
    //Количество доступных товаров
    //Количество забронированных товаров
    //Количество проданных товаров
 //Этот запрос извлекает данные из таблицы продуктов и преобразует их в объект StatusDistribution прямо в базе данных.
    @Query("""
        SELECT new com.example.musicstore.models.StatusDistribution(
            COUNT(CASE WHEN p.status = 'AVAILABLE' THEN 1 END),
            COUNT(CASE WHEN p.status = 'BOOKED' THEN 1 END),
            COUNT(CASE WHEN p.status = 'SOLD' THEN 1 END)
        )
        FROM Product p
    """)
    com.example.musicstore.models.StatusDistribution getStatusDistribution();

    //@Query — это аннотация Spring Data JPA, которая позволяет создавать кастомные SQL/JPQL запросы для методов репозитория.
    // Новые методы для статистики
    Long countByStatus(Product.ProductStatus status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'SOLD' AND p.buyer IS NOT NULL")
    Long countSoldProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'BOOKED'")
    Long countBookedProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'AVAILABLE'")
    Long countAvailableProducts();

 List<Product> findByNameContainingIgnoreCase(String searchTerm);

 List<Product> findByName(String trim);
}