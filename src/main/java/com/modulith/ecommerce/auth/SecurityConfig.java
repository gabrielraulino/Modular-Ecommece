package com.modulith.ecommerce.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("GET", "/products", "/products/{id}").permitAll()

                        // User endpoints (requires USER or ADMIN role)
                        .requestMatchers("GET", "/orders/user/{id}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("POST", "/orders/{id}/cancel").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("GET", "/carts/user/{id}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("POST", "/carts/user/{userId}/checkout").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("PUT", "/carts").hasAnyRole("USER", "ADMIN")

                        // Admin endpoints (requires ADMIN role)
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("POST", "/products").hasRole("ADMIN")
                        .requestMatchers("PUT", "/products/{id}").hasRole("ADMIN")
                        .requestMatchers("DELETE", "/products/{id}").hasRole("ADMIN")
                        .requestMatchers("PATCH", "/products/{id}/stock").hasRole("ADMIN")
                        .requestMatchers("GET", "/carts").hasRole("ADMIN")
                        .requestMatchers("GET", "/orders/all").hasRole("ADMIN")
                        .requestMatchers("GET", "/orders/{id}").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Configure as needed
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

