package com.plataformaeventos.web_backend.repository;

import com.plataformaeventos.web_backend.model.RefreshToken;
import com.plataformaeventos.web_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    
    @Modifying
    int deleteByUsuario(Usuario usuario); 
}