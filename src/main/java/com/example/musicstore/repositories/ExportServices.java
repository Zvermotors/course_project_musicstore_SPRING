package com.example.musicstore.repositories;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

public interface ExportServices {

    ByteArrayInputStream exportToExcel(LocalDate startDate, LocalDate endDate,
                                       String reportType, String authorFilter);

    ByteArrayInputStream exportToPdf(LocalDate startDate, LocalDate endDate,
                                     String reportType, String authorFilter);
}
