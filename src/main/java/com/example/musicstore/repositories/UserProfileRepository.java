package com.example.musicstore.repositories;

import com.example.musicstore.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByEmail(String email);//SELECT * FROM user_profile WHERE email = 'test@mail.ru';
    boolean existsByEmail(String email);//SELECT COUNT(u) > 0 FROM UserProfile u WHERE u.email = ?1

}

//findByEmail(String email)	               existsByEmail(String email)
//Найти и получить пользователя            Проверить существование пользователя
//Возвращает объект пользователя	       Возвращает true/false
//Для работы с данными пользователя	       Для проверки наличия пользователя