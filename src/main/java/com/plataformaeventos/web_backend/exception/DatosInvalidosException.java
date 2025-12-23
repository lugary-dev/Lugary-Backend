package com.plataformaeventos.web_backend.exception;

/**
 * Excepción utilizada para representar errores de validación
 * o datos de entrada incorrectos por parte del cliente.
 */
public class DatosInvalidosException extends BusinessException {

    public DatosInvalidosException(String message) {
        super(message);
    }
}