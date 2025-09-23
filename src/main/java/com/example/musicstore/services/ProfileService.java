// Объявление пакета, в котором находится класс
package com.example.musicstore.services;

// Импорт класса UserProfile из моделей
import com.example.musicstore.models.UserProfile;
// Импорт репозитория для работы с профилями пользователей
import com.example.musicstore.repositories.UserProfileRepository;
// Импорт исключения для случаев, когда сущность не найдена
import jakarta.persistence.EntityNotFoundException;
// Импорт аннотации для управления транзакциями
import jakarta.transaction.Transactional;
// Импорт аннотации для логирования
import lombok.extern.slf4j.Slf4j;
// Импорт аннотации для обозначения класса как сервиса Spring
import org.springframework.beans.factory.annotation.Autowired;
// Импорт аннотации Service
import org.springframework.stereotype.Service;

// Импорт класса для работы с датой и временем
import java.time.LocalDateTime;

// Аннотация для автоматического создания логгера
@Slf4j
// Аннотация для обозначения класса как сервисного компонента Spring
@Service
// Аннотация для управления транзакциями на уровне класса
@Transactional
// Объявление класса ProfileService

        /** Класс предоставляет бизнес-логику для работы с профилями пользователей,
        *выступая промежуточным слоем между контроллерами и репозиторием базы данных.
         */
public class ProfileService {

    // Объявление финального поля для репозитория профилей пользователей
    private final UserProfileRepository userProfileRepository;

    // Конструктор класса с автоматическим внедрением зависимости
    @Autowired
    // Конструктор, принимающий репозиторий профилей пользователей
    public ProfileService(UserProfileRepository userProfileRepository) {
        // Присвоение переданного репозитория полю класса
        this.userProfileRepository = userProfileRepository;
    }

    // Метод для проверки существования профиля по email
    //вызывается в ProfileController, делает запрос на существование пользователя в БД "userone"
    public boolean profileExists(String email) {
        // Возврат результата проверки существования профиля по email
        return userProfileRepository.existsByEmail(email);
    }

    // Метод для получения профиля текущего пользователя по email
    public UserProfile getCurrentUserProfile(String email) {
        // Поиск профиля по email и обработка случая, когда профиль не найден
        return userProfileRepository.findByEmail(email)
                // Если профиль не найден, выбрасывается исключение
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    // Метод для обновления профиля пользователя
    // в аргументы функции получаем форму и текущий email
    public UserProfile updateProfile(UserProfile updatedProfile, String currentUserEmail) {
        // 1. Находим профиль по email (а не по ID из формы)
        //Поиск существующего профиля по email текущего пользователя
        UserProfile existingProfile = userProfileRepository.findByEmail(currentUserEmail)
                // Если профиль не найден, выбрасывается исключение с сообщением
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for email: " + currentUserEmail));

        // 2. Логируем текущие и новые значения
        //Запись в лог отладочной информации о текущем и новом профиле
        log.debug("Updating profile. Current: {}, New: {}", existingProfile, updatedProfile);
        //получим:DEBUG - Updating profile. Current: UserProfile[id=1, email=user@mail.ru, name=Ivan], New: UserProfile[id=1, email=user@mail.ru, name=John]

        // 3. Обновляем только разрешенные поля
        //Обновление полного имени из переданного профиля
        existingProfile.setFullName(updatedProfile.getFullName());//берем из формы данные и обновляем
        //Обновление телефона из переданного профиля
        existingProfile.setPhone(updatedProfile.getPhone());
        //Обновление даты рождения из переданного профиля
        existingProfile.setBirthDate(updatedProfile.getBirthDate());

        // 4. Явно сохраняем и возвращаем результат
        //Сохранение и немедленная синхронизация с базой данных
        UserProfile savedProfile = userProfileRepository.saveAndFlush(existingProfile);
        //Запись в лог информации об успешном обновлении профиля
        log.info("Profile updated successfully. ID: {}, Email: {}", savedProfile.getId(), currentUserEmail);

        //Возврат сохраненного профиля
        return savedProfile;
    }


    //Метод для создания нового профиля пользователя
    //отвечает за создание нового профиля пользователя с проверкой уникальности email
    public UserProfile createNewUserProfile(UserProfile userProfile) {
        //Проверка существования профиля с таким же email
        if (userProfileRepository.existsByEmail(userProfile.getEmail())) {
            //Если профиль существует, выбрасывается исключение
            throw new IllegalArgumentException("Профиль с этим адресом электронной почты уже существует");
        }

        //Установка текущей даты и времени как даты регистрации
        userProfile.setRegistrationDate(LocalDateTime.now());//устанавливаем дату регистрации пользователя
        //Сохранение профиля в базу данных
        UserProfile savedProfile = userProfileRepository.save(userProfile);//Сохранение в базу данных
        //Запись в лог информации о создании нового профиля
        log.info("New profile created for email: {}", userProfile.getEmail());//Запись в лог об успешном создании профиля

        //Возврат сохраненного профиля
        return savedProfile;
    }
}