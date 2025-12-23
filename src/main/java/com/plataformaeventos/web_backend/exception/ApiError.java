package com.plataformaeventos.web_backend.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Representa el formato estándar de respuesta de error de la API.
 *
 * Este DTO permite devolver al cliente información consistente
 * ante errores controlados y excepciones globales.
 */
@Data
@Builder
public class ApiError {

    /**
     * Momento exacto en el que se produjo el error.
     */
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP asociado al error.
     */
    private int status;

    /**
     * Descripción corta del tipo de error HTTP (por ejemplo, "Bad Request").
     */
    private String error;

    /**
     * Mensaje explicativo orientado a desarrolladores/consumidores de la API.
     */
    private String message;

    /**
     * Ruta del endpoint que produjo el error.
     */
    private String path;
}