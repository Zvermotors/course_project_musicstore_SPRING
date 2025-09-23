// Пакет, в котором находится тестовый класс
package com.example.musicstore.controller;

// Импорт модели Product
import com.example.musicstore.models.Product;
// Импорт сервиса для работы с продуктами
import com.example.musicstore.repositories.ProductService;
// Импорт аннотаций JUnit для тестирования
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Импорт аннотаций Mockito для создания mock-объектов
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// Импорт Spring Model для передачи данных в представление
import org.springframework.ui.Model;

// Импорт статических методов для утверждений и работы с Mockito
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Аннотация для интеграции Mockito с JUnit 5
@ExtendWith(MockitoExtension.class)
// Объявление тестового класса для CatalogController
class CatalogControllerTest {

    // Создание mock-объекта для ProductService
    @Mock
    private ProductService productService;

    // Создание mock-объекта для Model
    @Mock
    private Model model;

    // Внедрение mock-объектов в тестируемый контроллер
    @InjectMocks
    private CatalogController catalogController;

    // Объявление тестового продукта
    private Product testProduct;

    // Метод, выполняемый перед каждым тестом
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
    }

    // Тест для метода showProductDetails - проверка возвращаемого представления
    @Test
    void showProductDetails_ShouldReturnProductDetailsView() {
        // Arrange (подготовка) - настройка поведения mock-объекта
        // Когда вызывается getProductById с аргументом 1L, возвращать testProduct
        when(productService.getProductById(1L)).thenReturn(testProduct);

        // Act (действие) - вызов тестируемого метода
        // Получение имени представления от контроллера
        String viewName = catalogController.showProductDetails(1L, model);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что возвращено правильное имя представления
        assertEquals("product-details", viewName);
        // Проверка, что метод getProductById был вызван с правильным аргументом
        verify(productService).getProductById(1L);
        // Проверка, что продукт был добавлен в модель
        verify(model).addAttribute("product", testProduct);
    }

    // Тест для метода aboutPage - проверка возвращаемого представления
    @Test
    void aboutPage_ShouldReturnAboutView() {
        // Act (действие) - вызов тестируемого метода
        // Получение имени представления "О нас"
        String viewName = catalogController.aboutPage(model);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что возвращено правильное имя представления
        assertEquals("about", viewName);
    }

    // Тест для проверки добавления продукта в модель при его существовании
    @Test
    void showProductDetails_WhenProductExists_ShouldAddProductToModel() {
        // Arrange (подготовка) - настройка поведения mock-объекта
        // Когда вызывается getProductById с аргументом 1L, возвращать testProduct
        when(productService.getProductById(1L)).thenReturn(testProduct);

        // Act (действие) - вызов тестируемого метода
        catalogController.showProductDetails(1L, model);

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что продукт был добавлен в модель
        verify(model).addAttribute("product", testProduct);
        // Проверка, что метод getProductById был вызван
        verify(productService).getProductById(1L);
    }

    // Тест для обработки исключения при отсутствии продукта
    @Test
    void showProductDetails_WhenProductNotExists_ShouldHandleException() {
        // Arrange (подготовка) - настройка поведения mock-объекта для выброса исключения
        // Когда вызывается getProductById с аргументом 1L, выбрасывать исключение
        when(productService.getProductById(1L)).thenThrow(new RuntimeException("Product not found"));

        // Act & Assert (действие и проверка) - проверка выброса исключения
        // Проверка, что при вызове метода выбрасывается RuntimeException
        assertThrows(RuntimeException.class, () ->
                catalogController.showProductDetails(1L, model)
        );

        // Проверка, что метод getProductById был вызван
        verify(productService).getProductById(1L);
    }
}