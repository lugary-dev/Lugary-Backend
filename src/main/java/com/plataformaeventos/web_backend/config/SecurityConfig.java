package com.plataformaeventos.web_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> {});
        
        http.csrf(csrf -> csrf.disable());

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(auth -> auth
                // Permitir todas las peticiones OPTIONS (preflight de CORS)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // --- RUTAS PÚBLICAS ---
                .requestMatchers(
                        "/api/hello",
                        "/api/usuarios",
                        "/api/auth/login",
                        "/api/auth/refresh-token" // [NUEVO] Permitir refrescar token sin estar autenticado (con access token)
                ).permitAll()

                // [NUEVO] Permitir que CUALQUIERA vea los espacios y su detalle
                .requestMatchers(HttpMethod.GET, "/api/espacios", "/api/espacios/**").permitAll()
                
                // --- RUTAS PRIVADAS ---
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
        );

        http.formLogin(form -> form.disable());
        http.httpBasic(Customizer.withDefaults());

        // Se añade el filtro JWT antes del filtro de autenticación por usuario/contraseña.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}