// Пакет, в котором находится класс
package com.example.musicstore.models;

// Импорт аннотации @Data из проекта Lombok
import lombok.Data;
// Импорт класса BigDecimal для точных денежных расчетов
import java.math.BigDecimal;
// Импорт класса HashMap для реализации хеш-таблицы
import java.util.HashMap;
// Импорт интерфейса Map для работы с коллекцией ключ-значение
import java.util.Map;

/**
 * Класс Cart представляет корзину покупок.
 * Управляет добавлением, удалением и изменением товаров в корзине,
 * а также рассчитывает общую стоимость.
 */
@Data // Аннотация Lombok, которая автоматически генерирует:
// - геттеры и сеттеры для всех полей
// - методы toString(), equals() и hashCode()
// - конструктор без аргументов
public class Cart {

    // Коллекция товаров в корзине, где ключ - ID товара, значение - объект CartItem
    private Map<Long, CartItem> items = new HashMap<>();

    // Общая сумма всех товаров в корзине, инициализирована нулем
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Добавляет товар в корзину или увеличивает его количество, если уже присутствует
     * @param product - товар для добавления
     */
    public void addItem(Product product) {
        // Получаем товар из корзины по ID
        CartItem item = items.get(product.getId());
        // Если товара нет в корзине
        if (item == null) {
            // Создаем новый элемент корзины
            item = new CartItem(product);
            // Добавляем в коллекцию
            items.put(product.getId(), item);
        } else {
            // Увеличиваем количество существующего товара на 1
            item.setQuantity(item.getQuantity() + 1);
        }
        // Пересчитываем общую сумму
        calculateTotal();
    }

    /**
     * Удаляет товар из корзины по его ID
     * @param productId - ID товара для удаления
     */
    public void removeItem(Long productId) {
        // Удаляем товар из коллекции
        items.remove(productId);
        // Пересчитываем общую сумму
        calculateTotal();
    }

    /**
     * Обновляет количество конкретного товара в корзине
     * @param productId - ID товара
     * @param quantity - новое количество
     */
    public void updateQuantity(Long productId, int quantity) {
        // Получаем товар из корзины
        CartItem item = items.get(productId);
        // Если товар найден
        if (item != null) {
            // Устанавливаем новое количество
            item.setQuantity(quantity);
            // Пересчитываем общую сумму
            calculateTotal();
        }
    }

    /**
     * Очищает корзину полностью
     */
    public void clear() {
        // Очищаем коллекцию товаров
        items.clear();
        // Сбрасываем общую сумму к нулю
        totalAmount = BigDecimal.ZERO;
    }

    /**
     * Приватный метод для расчета общей суммы корзины
     */
    private void calculateTotal() {
        // Используем Stream API для расчета:
        totalAmount = items.values().stream() // Получаем поток значений из коллекции
                // Для каждого элемента вычисляем: цена * количество
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                // Суммируем все значения, начиная с нуля
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Возвращает общее количество товаров в корзине
     * @return сумма количеств всех товаров
     */
    public int getTotalItems() {
        return items.values().stream() // Получаем поток значений из коллекции
                // Преобразуем каждый элемент в его количество
                .mapToInt(CartItem::getQuantity)
                // Суммируем все количества
                .sum();
    }
}
//Что делает класс Cart:
//
//Этот класс представляет корзину покупок в интернет-магазине и предоставляет:
//
//Основной функционал:
//
//Добавление товаров в корзину
//
//Удаление товаров из корзины
//
//Изменение количества товаров
//
//Очистка корзины
//
//Расчетные функции:
//
//Автоматический пересчет общей суммы при любых изменениях
//
//Подсчет общего количества товаров
//
//Точные денежные расчеты с использованием BigDecimal
//
//Структура данных:
//
//Использует HashMap для эффективного хранения и поиска товаров
//
//Каждый товар представлен объектом CartItem
//
//Класс используется для:
//
//Управления сессией покупок пользователя
//
//Временного хранения выбранных товаров
//
//Расчетов предварительной стоимости заказа
//
//Взаимодействия с пользовательским интерфейсом корзины