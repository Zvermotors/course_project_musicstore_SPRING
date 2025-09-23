// Объявление пакета, в котором находится тестовый класс
package com.example.musicstore.services;

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
// Импорт исключения для сущности не найденной в базе данных
import jakarta.persistence.EntityNotFoundException;
// Импорт аннотаций JUnit для тестирования
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Импорт аннотаций Mockito для создания mock-объектов
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// Импорт класса для работы с загружаемыми файлами
import org.springframework.web.multipart.MultipartFile;

// Импорт класса для обработки исключений ввода-вывода
import java.io.IOException;
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
// Объявление тестового класса для ProductService
class ProductServiceTest {

    // Создание mock-объекта для репозитория продуктов
    @Mock
    private ProductRepository productRepository;

    // Создание mock-объекта для репозитория пользователей
    @Mock
    private UserRepository userRepository;

    // Создание mock-объекта для репозитория заказов
    @Mock
    private OrderRepository orderRepository;

    // Создание mock-объекта для загружаемого файла
    @Mock
    private MultipartFile multipartFile;

    // Внедрение mock-объектов в тестируемый сервис
    @InjectMocks
    private ProductService productService;

    // Объявление тестового продукта
    private Product testProduct;
    // Объявление тестового пользователя
    private User testUser;

    // Метод, выполняемый перед каждым тестом для инициализации данных
    @BeforeEach
    void setUp() {
        // Создание нового экземпляра продукта
        testProduct = new Product();
        // Установка идентификатора продукта
        testProduct.setId(1L);
        // Установка названия продукта
        testProduct.setName("Test Product");
        // Установка цены продукта
        testProduct.setPrice(100.0);
        // Установка автора продукта (email пользователя)
        testProduct.setAuthor("author@example.com");

        // Создание нового экземпляра пользователя
        testUser = new User();
        // Установка идентификатора пользователя
        testUser.setId(1L);
        // Установка email пользователя
        testUser.setEmail("user@example.com");
    }

    // Тест для получения всех продуктов
    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается findAll(), возвращать список с тестовым продуктом
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        // Act (действие) - вызов тестируемого метода
        // Получение списка всех продуктов
        List<Product> result = productService.getAllProducts();

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что результат не null
        assertNotNull(result);
        // Проверка, что список содержит 1 элемент
        assertEquals(1, result.size());
        // Проверка, что возвращенный продукт соответствует ожидаемому
        assertEquals(testProduct, result.get(0));
        // Проверка, что метод findAll() был вызван
        verify(productRepository).findAll();
    }

    // Тест для получения продукта по ID, когда продукт существует
    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается findById(1L), возвращать Optional с тестовым продуктом
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act (действие) - вызов тестируемого метода
        // Получение продукта по ID
        Product result = productService.getProductById(1L);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что результат не null
        assertNotNull(result);
        // Проверка, что ID продукта соответствует ожидаемому
        assertEquals(1L, result.getId());
        // Проверка, что метод findById() был вызван с правильным аргументом
        verify(productRepository).findById(1L);
    }

    // Тест для получения продукта по ID, когда продукт не существует
    @Test
    void getProductById_WhenProductNotExists_ShouldThrowException() {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается findById(1L), возвращать пустой Optional
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert (действие и проверка) - проверка выброса исключения
        // Проверка, что при вызове метода выбрасывается EntityNotFoundException
        assertThrows(EntityNotFoundException.class, () ->
                productService.getProductById(1L)
        );
        // Проверка, что метод findById() был вызван
        verify(productRepository).findById(1L);
    }

    // Тест для поиска продуктов по названию
    @Test
    void findByTitleContaining_ShouldReturnMatchingProducts() {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается findByNameContainingIgnoreCase("test"), возвращать список с тестовым продуктом
        when(productRepository.findByNameContainingIgnoreCase("test"))
                .thenReturn(List.of(testProduct));

        // Act (действие) - вызов тестируемого метода
        // Поиск продуктов по названию
        List<Product> result = productService.findByTitleContaining("test");

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что результат не null
        assertNotNull(result);
        // Проверка, что список содержит 1 элемент
        assertEquals(1, result.size());
        // Проверка, что возвращенный продукт соответствует ожидаемому
        assertEquals(testProduct, result.get(0));
        // Проверка, что метод поиска был вызван с правильным аргументом
        verify(productRepository).findByNameContainingIgnoreCase("test");
    }

    // Тест для поиска продуктов, когда поисковый запрос пустой
    @Test
    void findByTitleContaining_WhenTitleIsEmpty_ShouldReturnAllProducts() {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается findAll(), возвращать список с тестовым продуктом
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        // Act (действие) - вызов тестируемого метода с пустой строкой
        // Поиск продуктов с пустым запросом
        List<Product> result = productService.findByTitleContaining("");

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что результат не null
        assertNotNull(result);
        // Проверка, что список содержит 1 элемент
        assertEquals(1, result.size());
        // Проверка, что метод findAll() был вызван вместо поиска по названию
        verify(productRepository).findAll();
    }

    // Тест для сохранения продукта без изображения
    @Test
    void saveProduct_WithoutImage_ShouldSaveProduct() throws IOException {
        // Arrange (подготовка) - настройка поведения mock-репозитория
        // Когда вызывается save() с любым продуктом, возвращать тестовый продукт
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act (действие) - вызов тестируемого метода с null вместо файла
        // Сохранение продукта без изображения
        productService.saveProduct(testProduct, null);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что метод save() был вызван с тестовым продуктом
        verify(productRepository).save(testProduct);
    }

    // Тест для удаления продукта
    @Test
    void deleteProduct_ShouldDeleteProductSuccessfully() throws IOException {
        // Arrange (подготовка) - настройка поведения mock-репозиториев
        // Когда вызывается findById(1L), возвращать Optional с тестовым продуктом
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        // Когда вызывается findByProductId(1L), возвращать пустой список заказов
        when(orderRepository.findByProductId(1L)).thenReturn(List.of());

        // Act (действие) - вызов тестируемого метода
        // Удаление продукта по ID
        productService.deleteProduct(1L);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что метод findById() был вызван для проверки существования продукта
        verify(productRepository).findById(1L);
        // Проверка, что метод поиска заказов был вызван для проверки связанных заказов
        verify(orderRepository).findByProductId(1L);
        // Проверка, что метод deleteById() был вызван для удаления продукта
        verify(productRepository).deleteById(1L);
    }
}