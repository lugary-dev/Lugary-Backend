package com.plataformaeventos.web_backend.service;

import com.plataformaeventos.web_backend.config.JwtService;
import com.plataformaeventos.web_backend.dto.LoginRequest;
import com.plataformaeventos.web_backend.dto.LoginResponse;
import com.plataformaeventos.web_backend.exception.DatosInvalidosException;
import com.plataformaeventos.web_backend.exception.RecursoNoEncontradoException;
import com.plataformaeventos.web_backend.model.Usuario;
import com.plataformaeventos.web_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe.")); // Mensaje específico

        // 2. Verificar si está activo
        if (!usuario.isActivo()) {
            throw new DatosInvalidosException("Tu cuenta está desactivada. Contacta al soporte.");
        }

        // 3. Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new DatosInvalidosException("La contraseña es incorrecta."); // Mensaje específico
        }

        // 4. Generar token
        String token = jwtService.generarToken(usuario.getEmail());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .userId(usuario.getId())
                .firstLogin(usuario.isFirstLogin())
                .build();
    }
}