// Объявление пакета, в котором находится перечисление
package com.example.musicstore.models.enums;

// Импорт интерфейса GrantedAuthority из Spring Security
// Этот интерфейс используется для представления прав/ролей в системе безопасности
import org.springframework.security.core.GrantedAuthority;

// Объявление перечисления Role, которое реализует интерфейс GrantedAuthority
// Это позволяет использовать роли непосредственно в механизме авторизации Spring Security
public enum Role implements GrantedAuthority {

    // Константа перечисления - роль обычного пользователя
    // Префикс ROLE_ является стандартным требованием Spring Security для ролей
    ROLE_USER,

    // Константа перечисления - роль администратора
    // Администратор имеет расширенные права доступа в системе
    ROLE_ADMIN;

    // Реализация метода getAuthority() из интерфейса GrantedAuthority
    // Этот метод возвращает строковое представление authority (роли)
    @Override
    public String getAuthority() {
        // Метод name() возвращает имя константы перечисления в виде строки
        // Например: ROLE_USER.name() вернет "ROLE_USER"
        return name();
    }
}