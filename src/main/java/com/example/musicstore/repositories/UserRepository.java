package com.example.musicstore.repositories;

import com.example.musicstore.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByActiveTrue(); // Добавляем этот метод




    @Query("SELECT u FROM User u ORDER BY u.id ASC")
    List<User> findAllByOrderByIdAsc();
}