package com.plataformaeventos.web_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración global de CORS para permitir que el frontend
 * (Vite en localhost:5173) llame a la API del backend.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origen del frontend (Vite)
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // Métodos permitidos (AGREGAMOS PATCH)
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        );

        // Headers permitidos
        config.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept")
        );

        // Permitir credenciales si en algún momento usamos cookies
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}