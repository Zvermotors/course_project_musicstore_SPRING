package com.example.musicstore.configurations;

import com.example.musicstore.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Публичные пути
                        .requestMatchers("/","/balance", "/cart","/login","/catalog", "/registration", "/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()

                        // Путь для перенаправления личного кабинета
                        .requestMatchers("/personal-account").authenticated()

                        // Только для ADMIN
                        .requestMatchers("/admin/**", "/admin/user-management","/admin-panel", "/admin/users", "/products", "/product-form").hasRole("ADMIN")

                        // Для всех аутентифицированных (и USER и ADMIN)
                        .requestMatchers("/home", "/catalog", "/profile","/cart", "/balance","/admin/user-management").authenticated()

                        // Все остальные запросы
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                response.sendRedirect("/admin-panel");
            } else {
                response.sendRedirect("/home");
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //метод  Аутентификации (Spring Security)  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    //находится в UserService
}