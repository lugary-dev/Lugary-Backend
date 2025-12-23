package com.plataformaeventos.web_backend.exception;

/**
 * Excepción base para representar errores de negocio controlados.
 *
 * Todas las excepciones específicas del dominio deberían extender
 * esta clase, de forma que el manejador global pueda distinguir
 * entre errores esperados y fallos inesperados.
 */
public class BusinessException extends RuntimeException {

    /**
     * Crea una nueva instancia de BusinessException con un mensaje descriptivo.
     *
     * @param message descripción legible del error de negocio.
     */
    public BusinessException(String message) {
        super(message);
    }
}