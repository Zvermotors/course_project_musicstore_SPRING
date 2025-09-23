// Объявление пакета, в котором находится класс
package com.example.musicstore.models;

// Импорт перечисления ролей пользователя
import com.example.musicstore.models.enums.Role;
// Импорт аннотаций JPA для работы с базой данных
import jakarta.persistence.*;
// Импорт аннотаций Lombok для автоматической генерации кода
import lombok.Data;
// Импорт интерфейсов и классов Spring Security
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Импорт класса для работы с большими десятичными числами (для баланса)
import java.math.BigDecimal;
// Импорт классов для работы с датой и временем
import java.time.LocalDateTime;
// Импорт классов для работы с коллекциями
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// Аннотация указывает, что этот класс является сущностью JPA
@Entity
// Аннотация задает имя таблицы в базе данных
@Table(name = "users")
// Аннотация Lombok - автоматически генерирует геттеры, сеттеры, toString, equals и hashCode
@Data   // При компиляции Lombok "видит" эту аннотацию. Генерирует байт-код с нужными методами toString(), equals(), hashCode() и конструктор
// Класс User реализует интерфейс UserDetails Spring Security для интеграции с системой аутентификации
public class User implements UserDetails {

    // Аннотация указывает, что это поле является первичным ключом
    @Id
    // Аннотация определяет стратегию генерации идентификатора (автоинкремент в базе данных)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email пользователя (используется как логин)
    // Аннотация указывает, что поле должно быть уникальным и не может быть null
    @Column(unique = true, nullable = false)
    private String email;

    // Пароль пользователя (должен храниться в зашифрованном виде)
    // Аннотация указывает, что поле не может быть null
    @Column(nullable = false)
    private String password;

    // Баланс виртуального счета пользователя
    // Аннотация определяет точность (10 цифр) и масштаб (2 знака после запятой) для денежных сумм
    @Column(name = "balance", precision = 10, scale = 2)
    // Инициализация баланса нулевым значением
    private BigDecimal balance = BigDecimal.ZERO; // Добавляем виртуальный счет

    // Флаг активности аккаунта
    private boolean active;

    // Коллекция ролей пользователя
    // Этот код использует аннотации JPA (Java Persistence API) для настройки связи между сущностью User и его ролями (Role).
    // Аннотация указывает, что это коллекция элементов (не сущностей)
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    // Аннотация определяет таблицу для хранения ролей и связь с пользователем
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    // Аннотация указывает, что enum должен сохраняться как строка
    @Enumerated(EnumType.STRING)
    // Аннотация определяет имя столбца для хранения ролей
    @Column(name = "role") // Явно указываем имя колонки как 'role'
    // Инициализация пустого множества ролей
    private Set<Role> roles = new HashSet<>();

    // Дата и время создания пользователя
    private LocalDateTime createdAt;
    // Дата и время последнего обновления пользователя
    private LocalDateTime updatedAt;

    // Метод, выполняемый перед сохранением сущности в базу данных
    // Аннотация JPA - callback метод перед persist операцией
    @PrePersist
    protected void onCreate() {
        // Установка текущей даты и времени при создании
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Метод, выполняемый перед обновлением сущности в базе данных
    // Аннотация JPA - callback метод перед update операцией
    @PreUpdate
    protected void onUpdate() {
        // Обновление времени последнего изменения
        updatedAt = LocalDateTime.now();
    }

    // Реализация метода из интерфейса UserDetails
    // Возвращает коллекцию прав/ролей пользователя
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Роли реализуют GrantedAuthority, поэтому возвращаем их напрямую
        return roles;
    }

    // Реализация метода из интерфейса UserDetails
    // Возвращает имя пользователя (в нашем случае - email)
    @Override
    public String getUsername() {
        return email;
    }

    // Сеттер для email с возможностью дополнительной логики
    public void setEmail(String email) {
        this.email = email;
    }

    // Реализация метода из интерфейса UserDetails
    // Проверяет, не истек ли срок действия аккаунта
    @Override
    public boolean isAccountNonExpired() {
        // В данной реализации аккаунт никогда не истекает
        return true;
    }

    // Реализация метода из интерфейса UserDetails
    // Проверяет, не заблокирован ли аккаунт
    @Override
    public boolean isAccountNonLocked() {
        // В данной реализации аккаунт никогда не блокируется через этот механизм
        return true;
    }

    // Реализация метода из интерфейса UserDetails
    // Проверяет, не истекли ли учетные данные (пароль)
    @Override
    public boolean isCredentialsNonExpired() {
        // В данной реализации учетные данные никогда не истекают
        return true;
    }

    // Реализация метода из интерфейса UserDetails
    // Проверяет, активен ли аккаунт
    @Override
    public boolean isEnabled() {
        // Возвращает значение поля active
        return active;
    }
    // Геттеры и сеттеры автоматически генерируются Lombok (@Data аннотация)
}