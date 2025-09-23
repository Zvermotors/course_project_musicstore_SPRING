// Пакет, в котором находится сервис
package com.example.musicstore.services;

// Импорт модели User
import com.example.musicstore.models.User;
// Импорт перечисления ролей пользователя
import com.example.musicstore.models.enums.Role;
// Импорт репозитория пользователей
import com.example.musicstore.repositories.UserRepository;
// Импорт интерфейса UserDetails из Spring Security
import org.springframework.security.core.userdetails.UserDetails;
// Импорт интерфейса UserDetailsService из Spring Security
import org.springframework.security.core.userdetails.UserDetailsService;
// Импорт исключения для случая, когда пользователь не найден
import org.springframework.security.core.userdetails.UsernameNotFoundException;
// Импорт аннотации для обозначения сервиса Spring
import org.springframework.stereotype.Service;

/**
 * Сервис для загрузки данных пользователя в Spring Security.
 * Реализует интерфейс UserDetailsService для интеграции с Spring Security.
 */
@Service // Аннотация указывает, что это компонент сервиса Spring
public class CustomUserDetailsService implements UserDetailsService {

    // Репозиторий для работы с пользователями в базе данных
    private final UserRepository userRepository;

    /**
     * Конструктор с внедрением зависимости UserRepository (Dependency Injection)
     * Это позволяет работать с базой данных для поиска пользователей
     * @param userRepository - репозиторий пользователей
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Основной метод, который вызывается Spring Security при попытке аутентификации.
     * Загружает данные пользователя по имени пользователя (email)
     * @param email - email пользователя (используется как username)
     * @return UserDetails - объект с данными пользователя для Spring Security
     * @throws UsernameNotFoundException - если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Ищет пользователя в БД по email
        User user = userRepository.findByEmail(email)
                // Если пользователь не найден, выбрасывает исключение
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));

        // Преобразование объекта User в UserDetails для Spring Security:
        return org.springframework.security.core.userdetails.User.builder()
                // Устанавливает имя пользователя (email)
                .username(user.getUsername())
                // Устанавливает пароль пользователя
                .password(user.getPassword())
                // Устанавливает authorities (роли и права) пользователя
                .authorities(
                        // Преобразует коллекцию ролей в массив строк
                        user.getRoles().stream()
                                // Преобразует каждую роль в её строковое представление
                                .map(Role::name) // Просто берём имя enum как есть
                                // Собирает в массив строк
                                .toArray(String[]::new)
                )
                // Создаёт immutable (неизменяемый) объект UserDetails
                .build();
    }
}
//Что делает класс CustomUserDetailsService:
//
//Этот сервис является ключевым компонентом интеграции Spring Security с вашим приложением. Он выполняет:
//
//Основные функции:
//
//Загрузка пользователей - получает данные пользователя из базы данных по email
//
//Преобразование данных - конвертирует вашу модель User в стандартный UserDetails Spring Security
//
//Обработка аутентификации - предоставляет данные для процесса аутентификации
//
//Ключевые особенности:
//
//Реализует UserDetailsService - стандартный интерфейс Spring Security
//
//Использует Dependency Injection - получает UserRepository через конструктор
//
//Обрабатывает исключения - выбрасывает UsernameNotFoundException если пользователь не найден
//
//Создает immutable объекты - обеспечивает безопасность через неизменяемость
//
//Процесс работы:
//
//Spring Security вызывает loadUserByUsername() при попытке входа
//
//Сервис ищет пользователя в базе данных по email
//
//Если пользователь найден, преобразует его в UserDetails
//
//Spring Security использует полученные данные для проверки пароля и прав
//
//Структура UserDetails:
//
//username - email пользователя
//
//password - хэшированный пароль
//
//authorities - роли пользователя (например, "ROLE_USER", "ROLE_ADMIN")
//
//Этот сервис обеспечивает безопасную интеграцию вашей пользовательской модели с механизмами аутентификации Spring Security.