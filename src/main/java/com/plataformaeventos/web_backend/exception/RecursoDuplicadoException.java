package com.plataformaeventos.web_backend.exception;

/**
 * Excepción utilizada cuando se intenta crear un recurso que
 * viola una restricción de unicidad, por ejemplo:
 *  - un email de usuario ya registrado.
 */
public class RecursoDuplicadoException extends BusinessException {

    public RecursoDuplicadoException(String message) {
        super(message);
    }
}