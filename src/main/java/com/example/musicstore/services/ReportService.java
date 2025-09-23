package com.example.musicstore.services;

import com.example.musicstore.models.*;
import com.example.musicstore.models.enums.OrderStatus;
import com.example.musicstore.repositories.OrderRepository;
import com.example.musicstore.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// Сервис для генерации отчетов и статистики
@Service // Помечаем класс как Spring Service компонент
@RequiredArgsConstructor // Lombok: автоматически создает конструктор с final полями
public class ReportService {

    // Репозитории для работы с заказами и продуктами
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;

    // Приватный метод для генерации отчета о выручке
    private RevenueReport generateRevenueReport(LocalDateTime start, LocalDateTime end) {
        RevenueReport report = new RevenueReport(); // Создаем объект отчета о выручке

        // Заменяем старый метод на новый подход - рассчитываем выручку по периодам
        List<RevenueByPeriod> revenueData = calculateRevenueByPeriod(start, end);
        report.setByPeriod(revenueData); // Устанавливаем данные по периодам в отчет

        // Расчет средней суммы чека
        BigDecimal totalRevenue = revenueData.stream() // Создаем поток из данных о выручке
                .map(RevenueByPeriod::getRevenue) // Преобразуем каждый период в его выручку
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Суммируем всю выручку

        Long totalSales = revenueData.stream() // Создаем поток из данных о выручке
                .mapToLong(RevenueByPeriod::getSalesCount) // Преобразуем каждый период в количество продаж
                .sum(); // Суммируем общее количество продаж

        // Если есть продажи, рассчитываем средний чек
        if (totalSales > 0) {
            report.setAverageCheck(totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP));
        }

        // Находим лучший день по выручке
        revenueData.stream() // Создаем поток из данных о выручке
                .max(Comparator.comparing(RevenueByPeriod::getRevenue)) // Находим период с максимальной выручкой
                .ifPresent(bestDay -> { // Если найден, устанавливаем данные о лучшем дне
                    report.setBestDay(bestDay.getPeriod());
                    report.setBestDayRevenue(bestDay.getRevenue());
                });

        return report; // Возвращаем сформированный отчет
    }

    // Приватный метод для генерации отчета по авторам
    private AuthorsReport generateAuthorsReport(LocalDateTime start, LocalDateTime end, String authorFilter) {
        AuthorsReport report = new AuthorsReport(); // Создаем объект отчета по авторам

        // Заменяем старый метод на ручной расчет - получаем статистику по авторам
        List<AuthorStats> authorStats = calculateAuthorStatsManually(start, end);

        // Если указан фильтр по автору, применяем его
        if (authorFilter != null && !authorFilter.isEmpty()) {
            authorStats = authorStats.stream() // Создаем поток из статистики авторов
                    .filter(stats -> stats.getAuthor().equals(authorFilter)) // Фильтруем по имени автора
                    .collect(Collectors.toList()); // Собираем обратно в список
        }

        report.setTopAuthors(authorStats); // Устанавливаем топ авторов в отчет
        report.setTotalAuthors((long) authorStats.size()); // Устанавливаем общее количество авторов

        return report; // Возвращаем сформированный отчет
    }

    // Приватный метод для генерации отчета о продажах
    private SalesReport generateSalesReport(LocalDateTime start, LocalDateTime end) {
        SalesReport report = new SalesReport(); // Создаем объект отчета о продажах

        // Заменяем старые методы на новые расчеты
        report.setByDay(calculateSalesByDay(start, end)); // Устанавливаем продажи по дням

        return report; // Возвращаем сформированный отчет
    }

    // ========== НОВЫЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Ручной расчет выручки по периодам
     */
    private List<RevenueByPeriod> calculateRevenueByPeriod(LocalDateTime start, LocalDateTime end) {
        // Получаем завершенные заказы за указанный период
        List<Order> completedOrders = orderRepository.findByStatusAndOrderDateBetween(
                OrderStatus.COMPLETED, start, end
        );

        // Группируем заказы по дате и рассчитываем статистику для каждого периода
        return completedOrders.stream() // Создаем поток из завершенных заказов
                .collect(Collectors.groupingBy( // Группируем заказы по дате
                        order -> order.getOrderDate().toLocalDate(), // Ключ группировки - дата заказа
                        Collectors.collectingAndThen(Collectors.toList(), orders -> { // Для каждой группы заказов
                            // Рассчитываем общую выручку для периода
                            BigDecimal revenue = orders.stream() // Создаем поток из заказов периода
                                    .map(Order::getTotalAmount) // Получаем сумму каждого заказа
                                    .reduce(BigDecimal.ZERO, BigDecimal::add); // Суммируем все заказы

                            long count = orders.size(); // Количество заказов в периоде

                            // Рассчитываем средний чек для периода
                            BigDecimal average = count > 0 ?
                                    revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) :
                                    BigDecimal.ZERO;

                            // Создаем объект для хранения данных периода
                            RevenueByPeriod period = new RevenueByPeriod();
                            period.setPeriod(orders.get(0).getOrderDate().toLocalDate()); // Дата периода
                            period.setRevenue(revenue); // Общая выручка
                            period.setSalesCount(count); // Количество продаж
                            period.setAverageOrderValue(average); // Средний чек
                            return period;
                        })
                ))
                .values().stream() // Получаем поток значений из мапы
                .sorted(Comparator.comparing(RevenueByPeriod::getPeriod)) // Сортируем по дате
                .collect(Collectors.toList()); // Собираем в список
    }

    /**
     * Ручной расчет статистики по авторам
     */
    private List<AuthorStats> calculateAuthorStatsManually(LocalDateTime start, LocalDateTime end) {
        // Получаем завершенные заказы за указанный период
        List<Order> completedOrders = orderRepository.findByStatusAndOrderDateBetween(
                OrderStatus.COMPLETED, start, end
        );

        // Группируем заказы по автору продукта и рассчитываем статистику
        var authorStatsMap = completedOrders.stream() // Создаем поток из завершенных заказов
                .filter(order -> order.getProduct() != null && order.getProduct().getAuthor() != null) // Фильтруем заказы с указанным автором
                .collect(Collectors.groupingBy( // Группируем заказы по автору
                        order -> order.getProduct().getAuthor(), // Ключ группировки - автор
                        Collectors.collectingAndThen(Collectors.toList(), orders -> { // Для каждой группы заказов автора
                            long salesCount = orders.size(); // Количество продаж автора

                            // Общая выручка от продаж автора
                            BigDecimal totalRevenue = orders.stream() // Создаем поток из заказов автора
                                    .map(Order::getTotalAmount) // Получаем сумму каждого заказа
                                    .reduce(BigDecimal.ZERO, BigDecimal::add); // Суммируем все заказы

                            // Создаем объект статистики автора
                            AuthorStats stats = new AuthorStats();
                            stats.setAuthor(orders.get(0).getProduct().getAuthor()); // Имя автора
                            stats.setSalesCount(salesCount); // Количество продаж
                            stats.setTotalRevenue(totalRevenue); // Общая выручка
                            stats.setMarketShare(0.0); // Временное значение, рассчитаем ниже
                            return stats;
                        })
                ));

        // Рассчитываем общую выручку для всех авторов (для расчета долей рынка)
        BigDecimal totalRevenue = authorStatsMap.values().stream() // Создаем поток из статистики авторов
                .map(AuthorStats::getTotalRevenue) // Получаем выручку каждого автора
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Суммируем всю выручку

        // Рассчитываем доли рынка для каждого автора
        authorStatsMap.values().forEach(stats -> { // Для каждой статистики автора
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) { // Если общая выручка положительная
                // Рассчитываем долю рынка в процентах
                double marketShare = stats.getTotalRevenue() // Выручка автора
                        .multiply(BigDecimal.valueOf(100)) // Умножаем на 100 для процентов
                        .divide(totalRevenue, 2, RoundingMode.HALF_UP) // Делим на общую выручку
                        .doubleValue(); // Преобразуем в double
                stats.setMarketShare(marketShare); // Устанавливаем долю рынка
            }
        });

        return authorStatsMap.values().stream() // Создаем поток из статистики авторов
                .sorted((a1, a2) -> a2.getTotalRevenue().compareTo(a1.getTotalRevenue())) // Сортируем по убыванию выручки
                .collect(Collectors.toList()); // Собираем в список
    }

    /**
     * Ручной расчет продаж по дням
     */
    private List<SalesByDay> calculateSalesByDay(LocalDateTime start, LocalDateTime end) {
        // Получаем завершенные заказы за указанный период
        List<Order> completedOrders = orderRepository.findByStatusAndOrderDateBetween(
                OrderStatus.COMPLETED, start, end
        );

        // Группируем заказы по дате и рассчитываем статистику для каждого дня
        return completedOrders.stream() // Создаем поток из завершенных заказов
                .collect(Collectors.groupingBy( // Группируем заказы по дате
                        order -> order.getOrderDate().toLocalDate(), // Ключ группировки - дата
                        Collectors.collectingAndThen(Collectors.toList(), orders -> { // Для каждой группы заказов дня
                            long salesCount = orders.size(); // Количество продаж за день

                            // Общая выручка за день
                            BigDecimal revenue = orders.stream() // Создаем поток из заказов дня
                                    .map(Order::getTotalAmount) // Получаем сумму каждого заказа
                                    .reduce(BigDecimal.ZERO, BigDecimal::add); // Суммируем все заказы

                            // Создаем объект для хранения данных дня
                            SalesByDay salesByDay = new SalesByDay();
                            salesByDay.setDate(orders.get(0).getOrderDate().toLocalDate()); // Дата
                            salesByDay.setSalesCount(salesCount); // Количество продаж
                            salesByDay.setRevenue(revenue); // Выручка
                            return salesByDay;
                        })
                ))
                .values().stream() // Получаем поток значений из мапы
                .sorted(Comparator.comparing(SalesByDay::getDate)) // Сортируем по дате
                .collect(Collectors.toList()); // Собираем в список
    }

    /**
     * Подсчет проданных товаров за период
     */
    private Long countSoldProducts(LocalDateTime start, LocalDateTime end) {
        // Получаем завершенные заказы за указанный период
        List<Order> completedOrders = orderRepository.findByStatusAndOrderDateBetween(
                OrderStatus.COMPLETED, start, end
        );

        // Суммируем quantity всех заказов (если quantity не указан, считаем как 1)
        return completedOrders.stream() // Создаем поток из завершенных заказов
                .mapToLong(order -> order.getQuantity() != null ? order.getQuantity() : 1) // Получаем quantity или 1
                .sum(); // Суммируем количество товаров
    }

    // Публичный метод для получения статистики дашборда
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats(); // Создаем объект статистики дашборда

        // Устанавливаем общее количество продуктов
        stats.setTotalProducts((long) productRepository.count());
        // Устанавливаем количество доступных продуктов
        stats.setAvailableProducts(productRepository.countAvailableProducts());
        // Устанавливаем количество забронированных продуктов
        stats.setBookedProducts(productRepository.countBookedProducts());
        // Устанавливаем количество проданных продуктов
        stats.setSoldProducts(productRepository.countSoldProducts());

        // Устанавливаем общее количество заказов
        stats.setTotalOrders(orderService.getTotalOrdersCount());
        // Устанавливаем количество ожидающих заказов
        stats.setPendingOrders(orderService.getOrdersCountByStatus(OrderStatus.PENDING));
        // Устанавливаем количество завершенных заказов
        stats.setCompletedOrders(orderService.getOrdersCountByStatus(OrderStatus.COMPLETED));

        // Устанавливаем общую выручку
        stats.setTotalRevenue(orderService.getTotalRevenue());

        return stats; // Возвращаем статистику
    }

    // Метод для генерации полного отчета с транзакционной поддержкой (только чтение)
    @Transactional(readOnly = true) // Аннотация для транзакции только для чтения
    public ReportData generateReport(LocalDate startDate, LocalDate endDate, String authorFilter) {
        // Преобразуем LocalDate в LocalDateTime (начало дня и конец дня)
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        ReportData reportData = new ReportData(); // Создаем объект данных отчета

        // Заполняем отчет различными видами статистики
        reportData.setDashboard(generateDashboardStats(start, end)); // Статистика дашборда
        reportData.setRevenue(generateRevenueReport(start, end)); // Отчет о выручке
        reportData.setAuthors(generateAuthorsReport(start, end, authorFilter)); // Отчет по авторам
        reportData.setSales(generateSalesReport(start, end)); // Отчет о продажах

        return reportData; // Возвращаем полный отчет
    }

    // Приватный метод для генерации статистики дашборда за период
    private DashboardStats generateDashboardStats(LocalDateTime start, LocalDateTime end) {
        DashboardStats stats = new DashboardStats(); // Создаем объект статистики

        // Устанавливаем общую выручку за период
        stats.setTotalRevenue(orderRepository.getTotalRevenueByPeriod(start, end));
        // Устанавливаем общее количество продаж за период
        stats.setTotalSales(orderRepository.countCompletedOrdersByPeriod(start, end));
        // Устанавливаем общее количество уникальных авторов
        stats.setTotalAuthors((long) productRepository.findAllDistinctAuthors().size());
        // Устанавливаем общее количество проданных товаров за период
        stats.setTotalProducts(countSoldProducts(start, end));

        // Добавляем статистику по статусам продуктов
        stats.setAvailableProducts(productRepository.countByStatus(Product.ProductStatus.AVAILABLE)); // Доступные
        stats.setBookedProducts(productRepository.countByStatus(Product.ProductStatus.BOOKED)); // Забронированные
        stats.setSoldProducts(productRepository.countByStatus(Product.ProductStatus.SOLD)); // Проданные

        return stats; // Возвращаем статистику
    }

    // Метод для получения списка всех уникальных авторов
    public List<String> getAllAuthors() {
        return productRepository.findAllDistinctAuthors(); // Возвращаем список авторов из репозитория
    }
}