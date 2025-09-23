// Объявление пакета, в котором находится тестовый класс
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
// Импорт аннотаций JUnit для тестирования
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// Импорт аннотации для расширения функциональности тестов
import org.junit.jupiter.api.extension.ExtendWith;
// Импорт аннотаций Mockito для создания mock-объектов
import org.mockito.InjectMocks;
import org.mockito.Mock;
// Импорт расширения Mockito для JUnit 5
import org.mockito.junit.jupiter.MockitoExtension;

// Импорт класса для работы с большими десятичными числами
import java.math.BigDecimal;
// Импорт класса для работы с датой и временем
import java.time.LocalDateTime;
// Импорт класса для работы со списками
import java.util.List;
// Импорт класса для работы с опциональными значениями
import java.util.Optional;

// Импорт статических методов для утверждений
import static org.junit.jupiter.api.Assertions.*;
// Импорт статических методов для работы с Mockito
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Аннотация для интеграции Mockito с JUnit 5
@ExtendWith(MockitoExtension.class)
// Объявление тестового класса для OrderService
class OrderServiceTest {

    // Создание mock-объекта для репозитория заказов
    @Mock
    private OrderRepository orderRepository;

    // Создание mock-объекта для репозитория продуктов
    @Mock
    private ProductRepository productRepository;

    // Создание mock-объекта для репозитория пользователей
    @Mock
    private UserRepository userRepository;

    // Создание mock-объекта для сервиса продуктов
    @Mock
    private ProductService productService;

    // Создание mock-объекта для сервиса email
    @Mock
    private EmailService emailService;

    // Внедрение mock-объектов в тестируемый сервис заказов
    @InjectMocks
    private OrderService orderService;

    // Объявление тестового продукта
    private Product testProduct;
    // Объявление тестового пользователя
    private User testUser;
    // Объявление тестового заказа
    private Order testOrder;

    // Метод, выполняемый перед каждым тестом для инициализации данных
    @BeforeEach
    void setUp() {
        // Создание нового экземпляра продукта
        testProduct = new Product();
        // Установка идентификатора продукта
        testProduct.setId(1L);
        // Установка названия продукта
        testProduct.setName("Test Guitar");
        // Установка цены продукта
        testProduct.setPrice(1000.0);

        // Создание нового экземпляра пользователя
        testUser = new User();
        // Установка идентификатора пользователя
        testUser.setId(1L);
        // Установка email пользователя
        testUser.setEmail("test@example.com");

        // Создание нового экземпляра заказа
        testOrder = new Order();
        // Установка идентификатора заказа
        testOrder.setId(1L);
        // Установка продукта для заказа
        testOrder.setProduct(testProduct);
        // Установка пользователя для заказа
        testOrder.setUser(testUser);
        // Установка количества товара в заказе
        testOrder.setQuantity(2);
        // Установка общей суммы заказа
        testOrder.setTotalAmount(BigDecimal.valueOf(2000.0));
        // Установка статуса заказа
        testOrder.setStatus(OrderStatus.PENDING);
        // Установка даты создания заказа
        testOrder.setOrderDate(LocalDateTime.now());
    }

    // Тест для успешного создания заказа
    @Test
    void createOrder_ShouldCreateOrderSuccessfully() {
        // Arrange (подготовка) - настройка поведения mock-репозиториев
        // Когда вызывается findById(1L) для продукта, возвращать Optional с тестовым продуктом
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        // Когда вызывается findById(1L) для пользователя, возвращать Optional с тестовым пользователем
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        // Когда вызывается save() с любым заказом, возвращать тестовый заказ
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act (действие) - вызов тестируемого метода
        // Создание заказа с указанием ID продукта, ID пользователя, количества и статуса
        Order result = orderService.createOrder(1L, 1L, 2, OrderStatus.PENDING);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что результат не null
        assertNotNull(result);
        // Проверка, что продукт в заказе соответствует ожидаемому
        assertEquals(testProduct, result.getProduct());
        // Проверка, что пользователь в заказе соответствует ожидаемому
        assertEquals(testUser, result.getUser());
        // Проверка, что количество товара соответствует ожидаемому
        assertEquals(2, result.getQuantity());
        // Проверка, что общая сумма заказа соответствует ожидаемой
        assertEquals(BigDecimal.valueOf(2000.0), result.getTotalAmount());
        // Проверка, что статус заказа соответствует ожидаемому
        assertEquals(OrderStatus.PENDING, result.getStatus());
        // Проверка, что дата заказа установлена
        assertNotNull(result.getOrderDate());

        // Проверка взаимодействий с mock-объектами
        // Проверка, что метод findById() был вызван для продукта
        verify(productRepository).findById(1L);
        // Проверка, что метод findById() был вызван для пользователя
        verify(userRepository).findById(1L);
        // Проверка, что метод save() был вызван для сохранения заказа
        verify(orderRepository).save(any(Order.class));
        // Проверка, что метод синхронизации статуса продукта был вызван
        verify(productService).syncProductStatusFromOrders(1L);
    }

    // Тест для проверки корректного расчета общей суммы заказа
    @Test
    void createOrder_ShouldCalculateTotalAmountCorrectly() {
        // Arrange (подготовка) - настройка поведения mock-репозиториев
        // Установка новой цены продукта для теста расчета
        testProduct.setPrice(500.0);
        // Когда вызывается findById(1L) для продукта, возвращать Optional с тестовым продуктом
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        // Когда вызывается findById(1L) для пользователя, возвращать Optional с тестовым пользователем
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Используем Answer чтобы вернуть реально созданный Order
        // Настройка сохранения заказа с возвратом переданного объекта
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            // Получение аргумента (заказа) из вызова метода save
            Order savedOrder = invocation.getArgument(0);
            // Возвращаем тот же объект, который был передан в save
            return savedOrder;
        });

        // Act (действие) - вызов тестируемого метода
        // Создание заказа с 3 единицами товара по цене 500
        Order result = orderService.createOrder(1L, 1L, 3, OrderStatus.PENDING);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка правильности расчета общей суммы: 500 * 3 = 1500
        assertEquals(BigDecimal.valueOf(1500.0), result.getTotalAmount());
    }

    // Тест для обработки ситуации, когда продукт не найден
    @Test
    void createOrder_WhenProductNotFound_ShouldThrowException() {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается findById(1L) для продукта, возвращать пустой Optional
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert (действие и проверка) - проверка выброса исключения
        // Проверка, что при вызове метода выбрасывается RuntimeException
        assertThrows(RuntimeException.class, () ->
                orderService.createOrder(1L, 1L, 2, OrderStatus.PENDING)
        );
    }

    // Тест для обработки ситуации, когда пользователь не найден
    @Test
    void createOrder_WhenUserNotFound_ShouldThrowException() {
        // Arrange (подготовка) - настройка поведения mock-репозиториев
        // Когда вызывается findById(1L) для продукта, возвращать Optional с тестовым продуктом
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        // Когда вызывается findById(1L) для пользователя, возвращать пустой Optional
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert (действие и проверка) - проверка выброса исключения
        // Проверка, что при вызове метода выбрасывается RuntimeException
        assertThrows(RuntimeException.class, () ->
                orderService.createOrder(1L, 1L, 2, OrderStatus.PENDING)
        );
    }

    // Тест для успешного обновления статуса заказа
    @Test
    void updateOrderStatus_ShouldUpdateStatusSuccessfully() {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается findById(1L) для заказа, возвращать Optional с тестовым заказом
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        // Когда вызывается save() с любым заказом, возвращать тестовый заказ
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act (действие) - вызов тестируемого метода
        // Обновление статуса заказа на COMPLETED
        Order result = orderService.updateOrderStatus(1L, OrderStatus.COMPLETED);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что статус заказа обновлен на COMPLETED
        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        // Проверка, что дата завершения заказа установлена
        assertNotNull(result.getCompletedDate());
        // Проверка, что метод findById() был вызван для заказа
        verify(orderRepository).findById(1L);
        // Проверка, что метод save() был вызван для сохранения изменений
        verify(orderRepository).save(any(Order.class));
        // Проверка, что метод синхронизации статуса продукта был вызван
        verify(productService).syncProductStatusFromOrders(1L);
    }

    // Тест для получения заказа по ID, когда заказ существует
    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается findById(1L) для заказа, возвращать Optional с тестовым заказом
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act (действие) - вызов тестируемого метода
        // Получение заказа по ID
        Order result = orderService.getOrderById(1L);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что результат не null
        assertNotNull(result);
        // Проверка, что ID заказа соответствует ожидаемому
        assertEquals(1L, result.getId());
        // Проверка, что метод findById() был вызван
        verify(orderRepository).findById(1L);
    }

    // Тест для получения заказа по ID, когда заказ не существует
    @Test
    void getOrderById_WhenOrderNotExists_ShouldReturnNull() {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается findById(1L) для заказа, возвращать пустой Optional
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act (действие) - вызов тестируемого метода
        // Попытка получения несуществующего заказа
        Order result = orderService.getOrderById(1L);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что результат null
        assertNull(result);
        // Проверка, что метод findById() был вызван
        verify(orderRepository).findById(1L);
    }

    // Тест для успешного удаления заказа
    @Test
    void deleteOrder_ShouldDeleteOrderSuccessfully() {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается findById(1L) для заказа, возвращать Optional с тестовым заказом
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act (действие) - вызов тестируемого метода
        // Удаление заказа по ID
        orderService.deleteOrder(1L);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что метод findById() был вызван для поиска заказа
        verify(orderRepository).findById(1L);
        // Проверка, что метод delete() был вызван с тестовым заказом
        verify(orderRepository).delete(testOrder);
    }
}