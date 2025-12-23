package com.plataformaeventos.web_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de entrada para la creación de reservas.
 *
 * Representa los datos mínimos necesarios para reservar un espacio
 * en un rango de fecha y hora determinado.
 */
@Data
public class ReservaCrearRequest {

    /**
     * Identificador del espacio que se desea reservar.
     */
    private Long espacioId;

    /**
     * Identificador del usuario que realiza la reserva.
     *
     * En futuras etapas se obtendrá del usuario autenticado.
     */
    private Long usuarioId;

    /**
     * Fecha y hora de inicio de la reserva.
     */
    private LocalDateTime fechaInicio;

    /**
     * Fecha y hora de fin de la reserva.
     */
    private LocalDateTime fechaFin;
}