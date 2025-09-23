// Объявление пакета, в котором находится тестовый класс
package com.example.musicstore.services;

// Импорт модели User
import com.example.musicstore.models.User;
// Импорт перечисления ролей пользователя
import com.example.musicstore.models.enums.Role;
// Импорт репозитория пользователей
import com.example.musicstore.repositories.UserRepository;
// Импорт аннотации Test из JUnit для обозначения тестовых методов
import org.junit.jupiter.api.Test;
// Импорт аннотации ExtendWith для расширения функциональности тестов
import org.junit.jupiter.api.extension.ExtendWith;
// Импорт аннотации InjectMocks для внедрения mock-объектов
import org.mockito.InjectMocks;
// Импорт аннотации Mock для создания mock-объектов
import org.mockito.Mock;
// Импорт расширения Mockito для JUnit 5
import org.mockito.junit.jupiter.MockitoExtension;
// Импорт интерфейса UserDetails из Spring Security
import org.springframework.security.core.userdetails.UserDetails;
// Импорт исключения для случая, когда пользователь не найден
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// Импорт класса для работы с опциональными значениями
import java.util.Optional;
// Импорт класса для работы с множествами (наборами)
import java.util.Set;

// Импорт статических методов для утверждений
import static org.junit.jupiter.api.Assertions.*;
// Импорт статических методов для работы с Mockito
import static org.mockito.Mockito.*;

// Аннотация для интеграции Mockito с JUnit 5
@ExtendWith(MockitoExtension.class)
// Объявление тестового класса для CustomUserDetailsService
class CustomUserDetailsServiceTest {

    // Создание mock-объекта для репозитория пользователей
    @Mock
    private UserRepository userRepository;

    // Внедрение mock-объектов в тестируемый сервис деталей пользователя
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    // Тест для загрузки данных пользователя по email, когда пользователь существует
    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // Arrange (подготовка) - настройка тестовых данных и поведения mock-объекта
        // Создание тестового пользователя
        User testUser = new User();
        // Установка email пользователя
        testUser.setEmail("test@example.com");
        // Установка закодированного пароля пользователя
        testUser.setPassword("encodedPassword");
        // Установка ролей пользователя (одна роль USER)
        testUser.setRoles(Set.of(Role.ROLE_USER));

        // Настройка поведения mock-репозитория: при поиске по email возвращать тестового пользователя
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act (действие) - вызов тестируемого метода
        // Загрузка данных пользователя по email (username в Spring Security - это email)
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что возвращенный объект UserDetails не null
        assertNotNull(userDetails);
        // Проверка, что username соответствует ожидаемому email
        assertEquals("test@example.com", userDetails.getUsername());
        // Проверка, что пароль соответствует ожидаемому закодированному паролю
        assertEquals("encodedPassword", userDetails.getPassword());
        // Проверка, что у пользователя есть ожидаемая роль ROLE_USER
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));

        // Проверка взаимодействия с mock-объектом
        // Проверка, что метод findByEmail() был вызван с правильным аргументом
        verify(userRepository).findByEmail("test@example.com");
    }

    // Тест для загрузки данных пользователя по email, когда пользователь не существует
    @Test
    void loadUserByUsername_WhenUserNotExists_ShouldThrowException() {
        // Arrange (подготовка) - настройка поведения mock-объекта
        // Настройка поведения mock-репозитория: при поиске по несуществующему email возвращать пустой Optional
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert (действие и проверка) - проверка выброса исключения
        // Проверка, что при вызове метода с несуществующим email выбрасывается UsernameNotFoundException
        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("nonexistent@example.com")
        );

        // Проверка взаимодействия с mock-объектом
        // Проверка, что метод findByEmail() был вызван с правильным аргументом
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    // Тест для загрузки данных пользователя с несколькими ролями
    @Test
    void loadUserByUsername_WhenUserHasMultipleRoles_ShouldReturnAllAuthorities() {
        // Arrange (подготовка) - настройка тестовых данных и поведения mock-объекта
        // Создание тестового пользователя с административными правами
        User testUser = new User();
        // Установка email пользователя
        testUser.setEmail("admin@example.com");
        // Установка закодированного пароля пользователя
        testUser.setPassword("encodedPassword");
        // Установка нескольких ролей пользователя (ADMIN и USER)
        testUser.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));

        // Настройка поведения mock-репозитория: при поиске по email возвращать тестового пользователя
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testUser));

        // Act (действие) - вызов тестируемого метода
        // Загрузка данных пользователя по email
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        // Assert (проверка) - проверка ожидаемых результатов
        // Проверка, что возвращенный объект UserDetails не null
        assertNotNull(userDetails);
        // Проверка, что у пользователя 2 роли (разрешения)
        assertEquals(2, userDetails.getAuthorities().size());
        // Проверка, что у пользователя есть роль ROLE_ADMIN
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        // Проверка, что у пользователя есть роль ROLE_USER
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }
}