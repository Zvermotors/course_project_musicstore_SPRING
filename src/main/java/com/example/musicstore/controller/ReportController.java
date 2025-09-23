package com.example.musicstore.controller;

import com.example.musicstore.models.ReportData;
import com.example.musicstore.services.ExportService;
import com.example.musicstore.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

//Этот класс `ReportController` в Spring-приложении отвечает за управление отчетами в музыкальном магазине. Он выполняет следующие функции:
//
//1. Отображает веб-страницу с отчетами (`showReportsPage`), которая возвращает HTML-шаблон.
//2. Обеспечивает API для получения данных отчетов в формате JSON (`getReportData`) по заданному диапазону дат и фильтру по автору.
//3. Предоставляет список всех авторов (`getAuthorsList`) для фронтенда.
//4. Позволяет экспортировать отчеты в файлы разных форматов (Excel, PDF) через REST-эндпоинт (`exportReport`), возвращая файлы пользователю для скачивания.
//5. Внутренний метод `validateDates` проверяет правильность диапазона дат.
@Controller
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ExportService exportService;

    //  метод для отображения reports HTML страницы
    @GetMapping
    public String showReportsPage() {
        return "reports"; //  вернет reports.html из templates/
    }


    //@ResponseBody - указывает, что возвращаемый объект должен быть
    // преобразован в JSON/XML и отправлен в теле HTTP-ответа
    @GetMapping("/data")
    @ResponseBody // аннотация для API методов
   //обертка для ответа, позволяющая контролировать HTTP статус, заголовки и тело
    public ResponseEntity<ReportData> getReportData(

           //@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) - преобразует строку в LocalDate (формат: YYYY-MM-DD)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String authorFilter) //извлекает параметры из URL строки запроса
    {

        validateDates(startDate, endDate);// Валидация дат

        ReportData reportData = reportService.generateReport(startDate, endDate, authorFilter);
        return ResponseEntity.ok(reportData);// Возврат успешного ответа
    }
//отображает авторов в формате json
    @GetMapping("/authors-list")
    @ResponseBody
    public ResponseEntity<List<String>> getAuthorsList() {
        List<String> authors = reportService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }

    //Этот метод представляет собой REST
    // endpoint для экспорта отчетов в различных форматах.
    @GetMapping("/export/{reportType}/{format}")
    @ResponseBody //указывает, что возвращаемые байты должны быть отправлены в теле HTTP-ответа
//ResponseEntity<byte[]> возвращает файл в виде массива байтов с HTTP заголовками
    public ResponseEntity<byte[]> exportReport(
            //PathVariable это аннотация Spring, которая извлекает данные из URL пути (не из параметров запроса).
            @PathVariable String reportType, // Тип отчета: sales, users, products и т.д.
            @PathVariable String format,  // Формат экспорта: excel, pdf, csv и т.д.
            //RequestParam метод "выловить" параметры, которые передаются в URL после знака вопроса, и использовать их внутри метода.
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,// Начальная дата (обязательная)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,// Конечная дата (обязательная)
            @RequestParam(required = false) String authorFilter) // Фильтр по автору (опциональный)
    {

        validateDates(startDate, endDate);// Валидация дат

        //Обработка формата Excel:
        if ("excel".equalsIgnoreCase(format)) {
            // Генерация Excel файла
            ByteArrayInputStream excelStream = exportService.exportToExcel(startDate, endDate, reportType, authorFilter);

            // Настройка HTTP заголовков
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // Бинарный поток
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(String.format("report-%s-%s.xlsx", startDate, endDate))
                    .build());// Заголовок для скачивания файла

            // Чтение байтов и возврат ответа
            try {
                return new ResponseEntity<>(excelStream.readAllBytes(), headers, HttpStatus.OK);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка экспорта Excel", e);
            }
        }

        //Обработка формата PDF:
        if ("pdf".equalsIgnoreCase(format)) {
            try {
                // Генерация PDF файла
                ByteArrayInputStream pdfStream = exportService.exportToPdf(startDate, endDate, reportType, authorFilter);

                // Настройка HTTP заголовков для PDF
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF); // MIME type для PDF
                headers.setContentDisposition(ContentDisposition.attachment()
                        .filename(String.format("report-%s-%s.pdf", startDate, endDate))
                        .build());// Заголовок для скачивания файла

                // Добавляем дополнительные заголовки для PDF
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

                // Чтение байтов и возврат ответа
                return new ResponseEntity<>(pdfStream.readAllBytes(), headers, HttpStatus.OK);

            } catch (Exception e) {
                throw new RuntimeException("Ошибка экспорта PDF", e);
            }
        }

        // Для неизвестных форматов возвращаем ошибку
        return ResponseEntity.badRequest()
                .body(("Unsupported format: " + format + ". Supported formats: excel, pdf").getBytes());
    }

    //проверка корректности даты
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Начальная дата не может быть позже конечной");
        }

        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Начальная дата не может быть в будущем");
        }
    }
}