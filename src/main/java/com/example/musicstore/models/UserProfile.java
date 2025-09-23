// Объявление пакета, в котором находится класс
package com.example.musicstore.models;

// Импорт аннотаций JPA для работы с базой данных
import jakarta.persistence.*;
// Импорт аннотаций Lombok для автоматической генерации кода
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
// Импорт аннотации Hibernate для оптимизации обновлений
import org.hibernate.annotations.DynamicUpdate;

// Импорт классов для работы с датой
import java.time.LocalDate;
import java.time.LocalDateTime;

// Аннотация Lombok - генерирует сеттеры для всех полей
@Setter
// Аннотация Lombok - генерирует геттеры для всех полей
@Getter
// Аннотация указывает, что этот класс является сущностью JPA
@Entity
// Аннотация задает имя таблицы в базе данных
@Table(name = "userone")
// Аннотация Hibernate - оптимизирует SQL UPDATE запросы, обновляя только измененные поля
@DynamicUpdate
// Класс `UserProfile` моделирует профиль пользователя в системе музыкального магазина.
public class UserProfile {
    // Геттеры и сеттеры

    // Аннотация указывает, что это поле является первичным ключом
    @Id
    // Аннотация определяет стратегию генерации идентификатора (автоинкремент в базе данных)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Полное имя пользователя
    // Аннотация определяет имя столбца и указывает, что поле не может быть null
    @Column(name = "full_name", nullable = false)
    private String fullName;

    // Email пользователя (уникальный идентификатор)
    // Аннотация указывает, что поле не может быть null и должно быть уникальным
    @Column(nullable = false, unique = true)
    private String email;

    // Номер телефона пользователя
    private String phone;

    // Дата рождения пользователя
    // Аннотация определяет имя столбца в таблице
    @Column(name = "birth_date")
    private LocalDate birthDate;

    // Дата и время регистрации пользователя
    // Аннотация определяет имя столбца и указывает, что поле не может быть обновлено
    @Column(name = "registration_date", updatable = false)
    private LocalDateTime registrationDate;
}
// Он содержит поля для хранения полного имени, уникального email, номера телефона,
// даты рождения и даты регистрации. Поле `id` является уникальным идентификатором,
// автоматически генерируемым при создании записи. Аннотация `@DynamicUpdate` оптимизирует
// обновление данных, обновляя только изменённые поля. В целом, этот класс хранит основные
// сведения о пользователе и связан с системой для персонализации и учета клиентов.