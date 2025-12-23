package com.plataformaeventos.web_backend.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de salida para la operación de login.
 *
 * Incluye el token JWT generado y los datos mínimos
 * del usuario autenticado para que el cliente pueda operar.
 */
@Data
@Builder
public class LoginResponse {

    /** Token JWT que deberá enviarse en Authorization: Bearer <token> */
    private String token;

    /** Tipo de token */
    private String tokenType;

    /** Email del usuario autenticado */
    private String email;

    /** Rol del usuario */
    private String rol;

    /** ID del usuario autenticado (NECESARIO PARA CREAR ESPACIOS) */
    private Long userId;   // 👈 AGREGADO
}