package com.plataformaeventos.web_backend.exception;

/**
 * Excepción utilizada para representar conflictos en las reservas,
 * como solapamientos de horarios o condiciones que impiden
 * confirmar una reserva en el estado actual del sistema.
 */
public class ConflictoReservaException extends BusinessException {

    public ConflictoReservaException(String message) {
        super(message);
    }
}