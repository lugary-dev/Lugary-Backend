package com.plataformaeventos.web_backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

/**
 * Servicio responsable de generar y validar tokens JWT.
 *
 * Este componente encapsula la lógica de construcción de tokens,
 * así como la extracción de información relevante (por ejemplo, el email).
 */
@Component
public class JwtService {

    private final Key signingKey;
    private final long expirationInMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationInMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationInMs = expirationInMs;
    }

    /**
     * Genera un nuevo token JWT para el usuario identificado por su email.
     *
     * @param email correo electrónico del usuario autenticado.
     * @return token JWT firmado.
     */
    public String generarToken(String email) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusMillis(expirationInMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(ahora))
                .setExpiration(Date.from(expiracion))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el correo electrónico (subject) del token.
     *
     * @param token token JWT recibido.
     * @return email del usuario.
     */
    public String extraerEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Valida que el token sea correcto y no se encuentre expirado.
     *
     * @param token token JWT a validar.
     * @return true si el token es válido, false en caso contrario.
     */
    public boolean esTokenValido(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}