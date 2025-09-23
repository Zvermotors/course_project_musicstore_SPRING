// Пакет, в котором находится сервис
package com.example.musicstore.services;

// Импорт модели Product
import com.example.musicstore.models.Product;
// Импорт репозитория продуктов
import com.example.musicstore.repositories.ProductRepository;
// Импорт исключения для случая, когда сущность не найдена
import jakarta.persistence.EntityNotFoundException;
// Импорт аннотации Lombok для генерации конструктора с final полями
import lombok.RequiredArgsConstructor;
// Импорт аннотации для обозначения сервиса Spring
import org.springframework.stereotype.Service;
// Импорт интерфейса ProductService
import com.example.musicstore.repositories.ProductService;

// Импорт интерфейса списка
import java.util.List;

/**
 * Реализация сервиса для работы с товарами (продуктами).
 * Предоставляет бизнес-логику для операций с товарами.
 */
@Service // Аннотация указывает, что это компонент сервиса Spring
@RequiredArgsConstructor // Аннотация Lombok генерирует конструктор для final полей
public class ProductServiceImpl implements ProductService {

    // Репозиторий для работы с товарами в базе данных
    private final ProductRepository productRepository;

    /**
     * Получает все товары из базы данных
     * @return список всех товаров
     */
    @Override
    public List<Product> getAllProducts() {
        // Вызывает метод репозитория для получения всех товаров
        return productRepository.findAll();
    }

    /**
     * Получает товар по его идентификатору
     * @param id - идентификатор товара
     * @return найденный товар
     * @throws EntityNotFoundException - если товар с указанным ID не найден
     */
    @Override
    public Product getProductById(Long id) {
        // Ищет товар в репозитории по ID
        return productRepository.findById(id)
                // Если товар не найден, выбрасывает исключение с сообщением
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }
}
//Что делает класс ProductServiceImpl:
//
//Этот класс является реализацией сервисного слоя для работы с товарами. Он выполняет:
//
//Основные функции:
//
//Получение всех товаров - возвращает полный список товаров из базы данных
//
//Поиск товара по ID - находит конкретный товар по его идентификатору
//
//Архитектурные особенности:
//
//Реализует интерфейс ProductService - следует принципу Dependency Inversion
//
//Использует Dependency Injection - получает ProductRepository через конструктор (генерируется Lombok)
//
//Следует принципу единственной ответственности - содержит только логику работы с товарами
//
//Обрабатывает исключения - корректно обрабатывает случай отсутствия товара
//
//Ключевые компоненты:
//
//@Service - помечает класс как Spring-сервис
//
//@RequiredArgsConstructor - автоматически генерирует конструктор для final полей
//
//productRepository - обеспечивает доступ к данным в базе
//
//EntityNotFoundException - стандартное исключение для случаев, когда сущность не найдена
//
//Процесс работы:
//
//Контроллер вызывает методы сервиса
//
//Сервис делегирует операции репозиторию
//
//Репозиторий выполняет запросы к базе данных
//
//Сервис обрабатывает результаты и возвращает данные контроллеру
//
//Обработка ошибок:
//
//При поиске несуществующего товара выбрасывает EntityNotFoundException
//
//Исключение содержит информативное сообщение с ID ненайденного товара
//
//Этот сервис обеспечивает абстракцию между контроллером и репозиторием, инкапсулируя бизнес-логику работы с товарами.