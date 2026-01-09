package com.plataformaeventos.web_backend.controller;

import com.plataformaeventos.web_backend.config.JwtService;
import com.plataformaeventos.web_backend.dto.AuthResponse;
import com.plataformaeventos.web_backend.dto.LoginRequest;
import com.plataformaeventos.web_backend.dto.LoginResponse;
import com.plataformaeventos.web_backend.model.RefreshToken;
import com.plataformaeventos.web_backend.model.Usuario;
import com.plataformaeventos.web_backend.repository.RefreshTokenRepository;
import com.plataformaeventos.web_backend.service.AuthService;
import com.plataformaeventos.web_backend.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        
        // Crear Refresh Token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getEmail());

        // Crear la Cookie HttpOnly
        ResponseCookie jwtCookie = ResponseCookie.from("refresh_token", refreshToken.getToken())
                .httpOnly(true)
                .secure(false) // Pon TRUE si usas HTTPS en producción
                .path("/api/auth/refresh-token")
                .maxAge(24 * 60 * 60) // 1 día en segundos
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
      @CookieValue(name = "refresh_token", required = false) String requestRefreshToken) {
        
        if (requestRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Refresh Token es requerido");
        }

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUsuario)
                .map(usuario -> {
                    String newJwt = jwtService.generarToken(usuario.getEmail());
                    return ResponseEntity.ok(new AuthResponse(newJwt));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token no está en base de datos!"));
    }
}