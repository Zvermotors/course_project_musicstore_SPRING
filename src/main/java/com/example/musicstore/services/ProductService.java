// Пакет, в котором находится сервис
package com.example.musicstore.services;

// Импорт модели Order
import com.example.musicstore.models.Order;
// Импорт модели Product
import com.example.musicstore.models.Product;
// Импорт модели User
import com.example.musicstore.models.User;
// Импорт перечисления статусов заказа
import com.example.musicstore.models.enums.OrderStatus;
// Импорт репозитория заказов
import com.example.musicstore.repositories.OrderRepository;
// Импорт репозитория продуктов
import com.example.musicstore.repositories.ProductRepository;
// Импорт репозитория пользователей
import com.example.musicstore.repositories.UserRepository;
// Импорт исключения для случая, когда сущность не найдена
import jakarta.persistence.EntityNotFoundException;
// Импорт аннотации для транзакционных операций
import jakarta.transaction.Transactional;
// Импорт аннотации Lombok для генерации конструктора с final полями
import lombok.RequiredArgsConstructor;
// Импорт аннотации Lombok для логирования
import lombok.extern.slf4j.Slf4j;
// Импорт аннотации для обозначения сервиса Spring
import org.springframework.stereotype.Service;
// Импорт класса для работы с загружаемыми файлами
import org.springframework.web.multipart.MultipartFile;

// Импорт классов для работы с файловой системой
import java.io.IOException;
// Импорт класса для точных денежных расчетов
import java.math.BigDecimal;
// Импорт классов для работы с файлами
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
// Импорт класса для опций копирования файлов
import java.nio.file.StandardCopyOption;
// Импорт класса для работы с датой и временем
import java.time.LocalDateTime;
// Импорт класса для пустых коллекций
import java.util.Collections;
// Импорт интерфейса списка
import java.util.List;
// Импорт класса для генерации UUID
import java.util.UUID;

/**
 * Сервис для работы с товарами (продуктами).
 * Содержит бизнес-логику для операций с товарами, включая бронирование, покупку и управление изображениями.
 */
@Slf4j // Аннотация Lombok для автоматического создания логгера
@Service // Аннотация указывает, что это компонент сервиса Spring
@RequiredArgsConstructor // Аннотация Lombok генерирует конструктор для final полей
public class ProductService {

    // Репозиторий для работы с товарами в базе данных
    private final ProductRepository productRepository;
    // Репозиторий для работы с пользователями в базе данных
    private final UserRepository userRepository;
    // Репозиторий для работы с заказами в базе данных
    private final OrderRepository orderRepository;

    /**
     * Метод сервиса, который возвращает все товары из базы данных.
     * @return список всех товаров
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Метод поиска товара по ID в модели Product, берем из БД products
     * @param id - идентификатор товара
     * @return найденный товар
     * @throws EntityNotFoundException - если товар не найден
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден"));
    }

    // Прописываем путь сохранения загруженных изображений
    public static final String UPLOAD_DIR = "static/uploads";

    /**
     * Сохранение товара в базу данных с одновременной загрузкой и обработкой изображения
     * @param product - товар для сохранения
     * @param imageFile - файл изображения
     * @throws IOException - если произошла ошибка при работе с файлом
     */
    public void saveProduct(Product product, MultipartFile imageFile) throws IOException {
        // Проверка наличия файла, проверяем, был ли передан файл, исключает пустые файлы
        if (imageFile != null && !imageFile.isEmpty()) {
            // Создает уникальное имя с UUID и временной меткой
            // Сохраняет оригинальное расширение файла
            String fileName = UUID.randomUUID() + "_" + System.currentTimeMillis() +
                    imageFile.getOriginalFilename().substring(imageFile.getOriginalFilename().lastIndexOf('.'));

            Path uploadPath = Paths.get("src/main/resources/" + UPLOAD_DIR) // Создает путь к директории для загрузки
                    .toAbsolutePath()
                    .normalize(); // Нормализует путь (убирает .. и т.д.)
            Files.createDirectories(uploadPath); // Создает директорию, если её нет

            // Сохраняет файл в исходную директорию
            Path targetPath = uploadPath.resolve(fileName);
            imageFile.transferTo(targetPath);

            // Копирует файл в целевую директорию для работы в runtime
            // Заменяет существующие файлы
            Path targetDir = Paths.get("target/classes/" + UPLOAD_DIR);
            Files.createDirectories(targetDir);
            Files.copy(targetPath, targetDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

            product.setImagePath(fileName);
        }
        // Сохраняет продукт в базу данных
        productRepository.save(product);
    }

    /**
     * Метод удаления товаров с транзакционной обработкой
     * @param id - идентификатор товара для удаления
     * @throws IOException - если произошла ошибка при удалении файла изображения
     */
    @Transactional
    public void deleteProduct(Long id) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Продукт с ID " + id + " не найден"));

        // 1. Сначала удаляем все заказы, связанные с этим товаром
        List<Order> orders = orderRepository.findByProductId(id);
        if (!orders.isEmpty()) {
            orderRepository.deleteAll(orders);
            log.info("Удалено {} заказов, связанных с товаром ID {}", orders.size(), id);
        }

        // 2. Затем удаляем изображение (если есть)
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                Path imagePath = Paths.get("." + product.getImagePath()).normalize().toAbsolutePath();
                if (Files.exists(imagePath)) {
                    Files.delete(imagePath);
                }
            } catch (IOException e) {
                throw new IOException("Не удалось удалить изображение продукта: " + product.getImagePath(), e);
            }
        }

        // 3. Теперь удаляем сам товар
        try {
            productRepository.deleteById(id);
        } catch (Exception e) {
            throw new IOException("Ошибка при удалении продукта из базы данных", e);
        }
    }

    /**
     * Метод бронирования товара с транзакционной обработкой
     * @param productId - идентификатор товара
     * @param username - имя пользователя, который бронирует
     */
    @Transactional
    public void bookProduct(Long productId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + username));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден: " + productId));

        // Проверка: нельзя бронировать собственный товар
        if (product.getAuthor().equals(user.getEmail())) {
            throw new IllegalStateException("Нельзя бронировать собственный товар");
        }

        if (product.getStatus() != Product.ProductStatus.AVAILABLE) {
            throw new IllegalStateException("Товар недоступен для бронирования");
        }

        // Создаем заказ для бронирования
        Order order = new Order();
        order.setProduct(product);
        order.setUser(user);
        order.setQuantity(1);
        order.setTotalAmount(BigDecimal.valueOf(product.getPrice()));
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // Обновляем продукт
        product.setStatus(Product.ProductStatus.BOOKED);
        product.setBookedBy(user);
        product.setBookingExpiry(LocalDateTime.now().plusDays(3));
        productRepository.save(product);
    }

    /**
     * Метод покупки товара с транзакционной обработкой
     * @param productId - идентификатор товара
     * @param username - имя пользователя, который покупает
     */
    @Transactional
    public void purchaseProduct(Long productId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + username));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден"));

        log.info("Попытка покупки. Текущий статус: {}, bookedBy: {}", product.getStatus(), product.getBookedBy());

        // Проверка: нельзя покупать собственный товар
        if (product.getAuthor().equals(user.getEmail())) {
            throw new IllegalStateException("Нельзя покупать собственный товар");
        }

        // Проверка брони
        if (product.getStatus() == Product.ProductStatus.BOOKED &&
                product.getBookedBy() != null &&
                !username.equals(product.getBookedBy().getEmail())) {
            throw new IllegalStateException("Этот товар забронирован другим пользователем");
        }

        // Создаем заказ для покупки
        Order order = new Order();
        order.setProduct(product);
        order.setUser(user);
        order.setQuantity(1);
        order.setTotalAmount(BigDecimal.valueOf(product.getPrice()));
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedDate(LocalDateTime.now());
        orderRepository.save(order);

        // Обновляем продукт
        product.setStatus(Product.ProductStatus.SOLD);
        product.setBuyer(user);
        product.setBookedBy(null);
        product.setBookingExpiry(null);
        productRepository.save(product);

        log.info("Товар продан. Новый статус: {}", product.getStatus());
    }

    /**
     * Метод отмены бронирования товара
     * @param productId - идентификатор товара
     * @param username - имя пользователя, который отменяет бронь
     */
    @Transactional
    public void cancelBooking(Long productId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + username));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден"));

        // Проверка прав: только тот кто бронировал, админ или владелец товара может отменить
        boolean canCancel = user.equals(product.getBookedBy()) ||
                user.getRoles().stream().anyMatch(role -> role.name().equals("ROLE_ADMIN")) ||
                product.getAuthor().equals(user.getEmail());

        if (!canCancel) {
            throw new IllegalStateException("Недостаточно прав для отмены брони");
        }

        if (product.getStatus() != Product.ProductStatus.BOOKED) {
            throw new IllegalStateException("Товар не забронирован");
        }

        // Находим и отменяем связанный заказ
        List<Order> orders = orderRepository.findByProductId(productId);
        orders.forEach(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        });

        // Обновляем продукт
        product.setStatus(Product.ProductStatus.AVAILABLE);
        product.setBookedBy(null);
        product.setBookingExpiry(null);
        productRepository.save(product);
    }

    /**
     * Синхронизация статуса товара на основе связанных заказов
     * @param productId - идентификатор товара
     */
    @Transactional
    public void syncProductStatusFromOrders(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        List<Order> orders = orderRepository.findByProductId(productId);

        if (!orders.isEmpty()) {
            // Берем статус из последнего заказа
            Order latestOrder = orders.get(orders.size() - 1);

            // Маппинг статусов Order -> Product
            Product.ProductStatus newProductStatus = mapOrderStatusToProductStatus(latestOrder.getStatus());

            if (product.getStatus() != newProductStatus) {
                product.setStatus(newProductStatus);
                productRepository.save(product);
                log.info("Синхронизирован статус продукта {}: {} -> {}",
                        productId, product.getStatus(), newProductStatus);
            }
        }
    }

    /**
     * Вспомогательный метод для маппинга статусов заказа в статусы товара
     * @param orderStatus - статус заказа
     * @return соответствующий статус товара
     */
    private Product.ProductStatus mapOrderStatusToProductStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case CONFIRMED, PENDING -> Product.ProductStatus.BOOKED;
            case COMPLETED -> Product.ProductStatus.SOLD;
            case CANCELLED -> Product.ProductStatus.AVAILABLE;
        };
    }

    /**
     * Поиск товаров по названию (без учета регистра)
     * @param title часть названия товара
     * @return список товаров, содержащих указанный текст в названии
     */
    public List<Product> findByTitleContaining(String title) {
        try {
            log.debug("Searching products with title containing: {}", title);

            if (title == null || title.trim().isEmpty()) {
                log.warn("Empty search title provided");
                return getAllProducts();
            }

            String searchTerm = title.trim().toLowerCase();
            List<Product> products = productRepository.findByNameContainingIgnoreCase(searchTerm);

            log.info("Found {} products containing '{}'", products.size(), searchTerm);
            return products;

        } catch (Exception e) {
            log.error("Error searching products with title: {}", title, e);
            throw new RuntimeException("Не удалось выполнить поиск товаров", e);
        }
    }

    /**
     * Альтернативный метод - поиск с точным совпадением
     * @param title точное название товара
     * @return список товаров с точным совпадением названия
     */
    public List<Product> findByName(String title) {
        try {
            log.debug("Searching products with exact name: {}", title);

            if (title == null || title.trim().isEmpty()) {
                log.warn("Empty product name provided");
                return Collections.emptyList();
            }

            List<Product> products = productRepository.findByName(title.trim());
            log.info("Found {} products with name '{}'", products.size(), title);
            return products;

        } catch (Exception e) {
            log.error("Error searching products with exact name: {}", title, e);
            throw new RuntimeException("Не удалось найти товары", e);
        }
    }
}
//Что делает класс ProductService:
//
//Этот класс является комплексным сервисом для управления товарами в музыкальном магазине. Он предоставляет:
//
//Основные функции:
//
//CRUD операции - получение, создание, обновление, удаление товаров
//
//Управление изображениями - загрузка, сохранение и удаление изображений товаров
//
//Бизнес-логика - бронирование, покупка, отмена брони товаров
//
//Поиск - поиск товаров по названию
//
//Синхронизация - синхронизация статусов товаров с заказами
//
//Ключевые особенности:
//
//Транзакционность - методы помечены @Transactional для обеспечения целостности данных
//
//Логирование - подробное логирование всех операций через SLF4J
//
//Обработка файлов - работа с файловой системой для управления изображениями
//
//Безопасность - проверки прав доступа и бизнес-правил
//
//Обработка ошибок - корректная обработка исключительных ситуаций
//
//Бизнес-правила:
//
//Нельзя бронировать/покупать собственный товар
//
//Проверка доступности товара перед бронированием
//
//Проверка прав при отмене бронирования
//
//Автоматическая установка срока бронирования (3 дня)
//
//Архитектурные паттерны:
//
//Сервисный слой с бизнес-логикой
//
//Dependency Injection через конструктор
//
//Разделение ответственности между методами
//
//Использование репозиториев для доступа к данным
//
//Этот сервис является центральным компонентом для управления товарами в приложении, обеспечивая всю необходимую бизнес-логику и интеграцию с другими компонентами системы.