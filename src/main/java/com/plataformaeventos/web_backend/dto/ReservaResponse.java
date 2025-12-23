package com.plataformaeventos.web_backend.dto;

import com.plataformaeventos.web_backend.model.EstadoReserva;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida para exponer información de reservas a los clientes.
 */
@Data
@Builder
public class ReservaResponse {

    private Long id;

    private Long espacioId;

    private Long usuarioId;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private BigDecimal precioTotal;

    private EstadoReserva estado;

    private LocalDateTime fechaCreacion;
}