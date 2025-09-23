//Этот класс представляет сервис для управления заказами и бронированием товаров.
package com.example.musicstore.services;

import com.example.musicstore.models.Product;
import com.example.musicstore.models.Product.ProductStatus;
import com.example.musicstore.models.User;
import com.example.musicstore.repositories.ProductRepository;
import com.example.musicstore.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Аннотация указывает, что этот класс является сервисным компонентом Spring
@Service
// Автоматически генерирует конструктор с обязательными аргументами (final поля)
@RequiredArgsConstructor
public class ProductOrderService {

    // Репозиторий для работы с товарами
    private final ProductRepository productRepository;
    // Репозиторий для работы с пользователями
    private final UserRepository userRepository;

    /**
     * Бронирование товара по email пользователя
     * @param productId ID товара для бронирования
     * @param email Email пользователя, который бронирует товар
     * @throws EntityNotFoundException если товар или пользователь не найдены
     * @throws IllegalStateException если товар недоступен для бронирования
     */
    @Transactional // Аннотация обеспечивает атомарность операции в рамках транзакции
    public void bookProduct(Long productId, String email) {
        // Поиск товара по ID, если не найден - выбрасывается исключение
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        // Поиск пользователя по email, если не найден - выбрасывается исключение
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверка, что товар доступен для бронирования
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new IllegalStateException("Product is not available");
        }

        // Установка статуса "Забронирован"
        product.setStatus(ProductStatus.BOOKED);
        // Установка пользователя, который забронировал товар
        product.setBookedBy(user);
        // Установка времени истечения брони (текущее время + 24 часа)
        product.setBookingExpiry(LocalDateTime.now().plusHours(24));
    }

    /**
     * Покупка товара по email пользователя
     * @param productId ID товара для покупки
     * @param email Email пользователя, который покупает товар
     * @throws EntityNotFoundException если товар или пользователь не найдены
     * @throws IllegalStateException если товар уже продан или забронирован другим пользователем
     */
    @Transactional
    public void purchaseProduct(Long productId, String email) {
        // Поиск товара по ID
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        // Поиск пользователя по email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверка, что товар еще не продан
        if (product.getStatus() == ProductStatus.SOLD) {
            throw new IllegalStateException("Product already sold");
        }

        // Проверка, что если товар забронирован, то только тем же пользователем
        if (product.getStatus() == ProductStatus.BOOKED &&
                !product.getBookedBy().getId().equals(user.getId())) {
            throw new IllegalStateException("Product booked by another user");
        }

        // Установка статуса "Продан"
        product.setStatus(ProductStatus.SOLD);
        // Установка покупателя товара
        product.setBuyer(user);
        // Сброс информации о бронировании
        product.setBookedBy(null);
        product.setBookingExpiry(null);
    }

    /**
     * Освобождение бронирования товара
     * @param productId ID товара для освобождения брони
     * @throws EntityNotFoundException если товар не найден
     */
    @Transactional
    public void releaseBooking(Long productId) {
        // Поиск товара по ID
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Освобождение брони только если товар в статусе "Забронирован"
        if (product.getStatus() == ProductStatus.BOOKED) {
            // Возврат статуса "Доступен"
            product.setStatus(ProductStatus.AVAILABLE);
            // Сброс информации о бронировании
            product.setBookedBy(null);
            product.setBookingExpiry(null);
        }
    }

    /**
     * Проверка и освобождение истекших бронирований
     * Метод предназначен для вызова по расписанию (например, через @Scheduled)
     */
    @Transactional
    public void checkExpiredBookings() {
        // Поиск всех забронированных товаров с истекшим временем брони
        productRepository.findAllByStatusAndBookingExpiryBefore(
                ProductStatus.BOOKED, // Статус "Забронирован"
                LocalDateTime.now() // Текущее время (время истечения должно быть раньше)
        ).forEach(product -> {
            // Для каждого найденного товара с истекшей броней
            // Возврат статуса "Доступен"
            product.setStatus(ProductStatus.AVAILABLE);
            // Сброс информации о бронировании
            product.setBookedBy(null);
            product.setBookingExpiry(null);
        });
    }
}
//Основная функциональность класса:
//Бронирование товара - пользователь может забронировать доступный товар на 24 часа
//
//Покупка товара - пользователь может купить товар (с проверкой прав на забронированный товар)
//
//Освобождение брони - ручное снятие бронирования с товара
//
//Автоматическая проверка истекших бронирований - метод для периодического освобождения просроченных броней
//
//Класс обеспечивает целостность данных через транзакции и проверки бизнес-логики.