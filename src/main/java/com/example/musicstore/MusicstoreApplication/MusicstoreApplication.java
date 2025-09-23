package com.example.musicstore.MusicstoreApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication(scanBasePackages = {
		"com.example.musicstore.configurations",
		"com.example.musicstore.models",
		"com.example.musicstore.controller",
		"com.example.musicstore.services",
		"com.example.musicstore.repositories",
		"com.example.resources.templates"
})
@EnableJpaRepositories("com.example.musicstore.repositories")  // ← Добавьте это
@EntityScan("com.example.musicstore.models")                 // ← И это
public class MusicstoreApplication {
	public static void main(String[] args) {
		SpringApplication.run(MusicstoreApplication.class, args);

	}

}