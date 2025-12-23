package com.plataformaeventos.web_backend.model;

/**
 * Representa el estado actual de una reserva.
 *
 * PENDIENTE → La reserva ha sido creada pero aún no confirmada.
 * CONFIRMADA → La reserva ha sido aceptada y está activa.
 * CANCELADA → La reserva ha sido anulada por el usuario o el sistema.
 */
public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA
}