package com.example.musicstore.services;

import com.example.musicstore.repositories.ExportServices;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import com.example.musicstore.models.*;
import com.example.musicstore.services.ReportService;
import lombok.RequiredArgsConstructor;
import com.example.musicstore.models.ReportData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

/**
 * Сервис для экспорта отчетов в различные форматы (Excel, PDF)
 */
@Service
@RequiredArgsConstructor
public class ExportService implements ExportServices {

    private final ReportService reportService;

    /**
     * Экспортирует данные в Excel формат
     * @param startDate начальная дата периода отчета
     * @param endDate конечная дата периода отчета
     * @param reportType тип отчета (revenue, authors, sales, all)
     * @param authorFilter фильтр по автору
     * @return поток байтов с Excel файлом
     */
    public ByteArrayInputStream exportToExcel(LocalDate startDate, LocalDate endDate, String reportType, String authorFilter) {
        // Генерируем данные отчета через сервис отчетов
        ReportData reportData = reportService.generateReport(startDate, endDate, authorFilter);

        try (Workbook workbook = new XSSFWorkbook()) {
            // Создаем соответствующие листы в зависимости от типа отчета
            switch (reportType.toLowerCase()) {
                case "revenue" -> createRevenueSheet(workbook, reportData.getRevenue());
                case "authors" -> createAuthorsSheet(workbook, reportData.getAuthors());
                case "sales" -> createSalesSheet(workbook, reportData.getSales());
                case "all" -> {
                    createRevenueSheet(workbook, reportData.getRevenue());
                    createAuthorsSheet(workbook, reportData.getAuthors());
                    createSalesSheet(workbook, reportData.getSales());
                }
            }

            // Записываем workbook в выходной поток
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Ошибка генерации Excel", e);
        }
    }

    /**
     * Создает лист с данными о выручке
     * @param workbook книга Excel для добавления листа
     * @param revenue данные о выручке
     */
    public void createRevenueSheet(Workbook workbook, RevenueReport revenue) {
        // Создаем новый лист "Выручка"
        Sheet sheet = workbook.createSheet("Выручка");

        // Создаем заголовки столбцов
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Дата", "Выручка", "Кол-во продаж", "Средний чек"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Заполняем данными по периодам
        int rowNum = 1;
        for (RevenueByPeriod period : revenue.getByPeriod()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(period.getPeriod().toString());
            row.createCell(1).setCellValue(period.getRevenue().doubleValue());
            row.createCell(2).setCellValue(period.getSalesCount());
            row.createCell(3).setCellValue(period.getAverageOrderValue().doubleValue());
        }

        // Добавляем итоговую строку
        Row totalRow = sheet.createRow(rowNum);
        totalRow.createCell(0).setCellValue("ИТОГО:");
        totalRow.createCell(1).setCellValue(revenue.getByPeriod().stream()
                .map(RevenueByPeriod::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
        totalRow.createCell(2).setCellValue(revenue.getByPeriod().stream()
                .mapToLong(RevenueByPeriod::getSalesCount)
                .sum());
    }

    /**
     * Создает лист с данными об авторах
     * @param workbook книга Excel для добавления листа
     * @param authors данные об авторах
     */
    private void createAuthorsSheet(Workbook workbook, AuthorsReport authors) {
        Sheet sheet = workbook.createSheet("Авторы");

        // Заголовки столбцов
        String[] headers = {"Автор", "Продажи", "Выручка", "Доля рынка (%)"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Заполняем данными по авторам
        int rowNum = 1;
        for (AuthorStats author : authors.getTopAuthors()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(author.getAuthor());
            row.createCell(1).setCellValue(author.getSalesCount());
            row.createCell(2).setCellValue(author.getTotalRevenue().doubleValue());
            row.createCell(3).setCellValue(author.getMarketShare());
        }
    }

    /**
     * Создает лист с данными о продажах
     * @param workbook книга Excel для добавления листа
     * @param sales данные о продажах
     */
    public void createSalesSheet(Workbook workbook, SalesReport sales) {
        Sheet sheet = workbook.createSheet("Продажи");

        // Заголовки столбцов
        String[] headers = {"Дата", "Продажи", "Выручка"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Заполняем данными по дням
        int rowNum = 1;
        for (SalesByDay day : sales.getByDay()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(day.getDate().toString());
            row.createCell(1).setCellValue(day.getSalesCount());
            row.createCell(2).setCellValue(day.getRevenue().doubleValue());
        }
    }

    /**
     * Экспортирует данные в PDF формат
     * @param startDate начальная дата периода отчета
     * @param endDate конечная дата периода отчета
     * @param reportType тип отчета
     * @param authorFilter фильтр по автору
     * @return поток байтов с PDF файлом
     */
    @Override
    public ByteArrayInputStream exportToPdf(LocalDate startDate, LocalDate endDate,
                                            String reportType, String authorFilter) {

        // Генерируем данные отчета
        ReportData reportData = reportService.generateReport(startDate, endDate, authorFilter);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PDDocument document = new PDDocument()) {

            // Создаем новую страницу в документе
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                // Устанавливаем шрифт и размер для заголовка
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700);

                // Выводим заголовок отчета на английском (из-за проблем с кириллицей)
                contentStream.showText("Report: " + getReportTitleEn(reportType));

                // Выводим период отчета
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(0, -30);
                contentStream.showText("Period: " + startDate + " - " + endDate);
                contentStream.newLineAtOffset(0, -25);

                // Выводим данные из dashboard
                if (reportData.getDashboard() != null) {
                    contentStream.showText("Total products: " +
                            getSafeValue(reportData.getDashboard().getTotalProducts(), false));
                    contentStream.newLineAtOffset(0, -25);

                    contentStream.showText("Available: " +
                            getSafeValue(reportData.getDashboard().getAvailableProducts(), false));
                    contentStream.newLineAtOffset(0, -25);

                    contentStream.showText("Booked: " +
                            getSafeValue(reportData.getDashboard().getBookedProducts(), false));
                    contentStream.newLineAtOffset(0, -25);

                    contentStream.showText("Sold: " +
                            getSafeValue(reportData.getDashboard().getSoldProducts(), false));
                    contentStream.newLineAtOffset(0, -25);

                    contentStream.showText("Total orders: " +
                            getSafeValue(reportData.getDashboard().getTotalOrders(), false));
                    contentStream.newLineAtOffset(0, -25);

                    contentStream.showText("Pending orders: " +
                            getSafeValue(reportData.getDashboard().getPendingOrders(), false));
                    contentStream.newLineAtOffset(0, -25);

                    contentStream.showText("Completed: " +
                            getSafeValue(reportData.getDashboard().getCompletedOrders(), false));
                    contentStream.newLineAtOffset(0, -25);

                    contentStream.showText("Revenue: " +
                            getSafeValue(reportData.getDashboard().getTotalRevenue(), true));
                    contentStream.newLineAtOffset(0, -25);

                    contentStream.showText("Total sales: " +
                            getSafeValue(reportData.getDashboard().getTotalSales(), false));
                    contentStream.newLineAtOffset(0, -25);

                    contentStream.showText("Authors: " +
                            getSafeValue(reportData.getDashboard().getTotalAuthors(), false));
                }

                contentStream.endText(); // Завершаем блок текста
            }

            // Сохраняем документ в выходной поток
            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("PDF export error", e);
        }
    }

    /**
     * Вспомогательный метод для безопасного получения значений с проверкой на кириллицу
     * @param value значение для форматирования
     * @param isCurrency является ли значение денежной суммой
     * @return отформатированная строка значения
     */
    private String getSafeValue(Object value, boolean isCurrency) {
        if (value == null) {
            return "0";
        }

        String result;
        if (value instanceof BigDecimal) {
            result = ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP).toString();
            if (isCurrency) {
                result += " RUB"; // Используем RUB вместо руб. из-за проблем с кириллицей
            }
        } else {
            result = value.toString();
        }

        // Проверяем на наличие кириллицы и заменяем если есть
        if (containsCyrillic(result)) {
            return "N/A"; // возвращаем "N/A" если обнаружена кириллица
        }

        return result;
    }

    /**
     * Проверяет строку на наличие кириллических символов
     * @param text строка для проверки
     * @return true если содержит кириллицу, иначе false
     */
    private boolean containsCyrillic(String text) {
        if (text == null) return false;
        return text.chars().anyMatch(ch -> ch >= 0x0400 && ch <= 0x04FF);
    }

    /**
     * Возвращает английское название типа отчета
     * @param reportType тип отчета
     * @return английское название отчета
     */
    private String getReportTitleEn(String reportType) {
        switch (reportType.toLowerCase()) {
            case "sales": return "Sales";
            case "users": return "Users";
            case "products": return "Products";
            case "revenue": return "Revenue";
            case "authors": return "Authors";
            default: return "General Report";
        }
    }

    /**
     * Перегруженный метод для безопасного получения значений
     * @param value значение для форматирования
     * @return отформатированная строка значения
     */
    private String getSafeValue(Object value) {
        if (value == null) {
            return "0";
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP) + " RUB";
        }
        return value.toString();
    }

    /**
     * Возвращает русское название типа отчета
     * @param reportType тип отчета
     * @return русское название отчета
     */
    private String getReportTitle(String reportType) {
        switch (reportType.toLowerCase()) {
            case "sales": return "Продажи";
            case "users": return "Пользователи";
            case "products": return "Товары";
            case "revenue": return "Выручка";
            case "authors": return "Авторы";
            default: return "Общий отчет";
        }
    }

    /**
     * Перегруженный метод для безопасного получения числовых значений
     * @param value числовое значение
     * @return строковое представление числа
     */
    private String getSafeValue(Number value) {
        if (value == null) {
            return "0";
        }
        return value.toString();
    }

    /**
     * Перегруженный метод для безопасного получения денежных значений
     * @param value денежное значение
     * @return отформатированная денежная сумма
     */
    private String getSafeValue(BigDecimal value) {
        if (value == null) {
            return "0.00 руб.";
        }
        return value.setScale(2, RoundingMode.HALF_UP) + " руб.";
    }
}