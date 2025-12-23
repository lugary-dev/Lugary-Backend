package com.plataformaeventos.web_backend.exception;

/**
 * Excepción utilizada para indicar que un recurso solicitado
 * no existe en el sistema (por ejemplo, un usuario o espacio).
 */
public class RecursoNoEncontradoException extends BusinessException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}