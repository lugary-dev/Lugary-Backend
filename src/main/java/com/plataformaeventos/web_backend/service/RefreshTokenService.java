package com.plataformaeventos.web_backend.service;

import com.plataformaeventos.web_backend.model.RefreshToken;
import com.plataformaeventos.web_backend.model.Usuario;
import com.plataformaeventos.web_backend.repository.RefreshTokenRepository;
import com.plataformaeventos.web_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refresh-expiration}")
    private Long refreshDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public RefreshToken createRefreshToken(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. PRIMERO: Borramos cualquier token viejo que tenga este usuario
        // Esto evita el error de "Duplicate Entry" o "Unique Constraint"
        refreshTokenRepository.deleteByUsuario(usuario);
        
        // Forzar el borrado inmediato para liberar la restricción
        refreshTokenRepository.flush(); 

        // 2. SEGUNDO: Creamos el nuevo token
        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshDurationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expirado. Por favor inicie sesión nuevamente.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        usuarioRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUsuario);
    }
}