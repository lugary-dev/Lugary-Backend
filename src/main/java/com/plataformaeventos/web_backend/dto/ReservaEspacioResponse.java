package com.plataformaeventos.web_backend.dto;

import com.plataformaeventos.web_backend.model.EstadoReserva;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de salida para listar las reservas asociadas a un espacio
 * (vista del propietario).
 */
@Data
@Builder
public class ReservaEspacioResponse {

    private Long id;

    private Long usuarioId;

    private String nombreUsuario;

    private String emailUsuario;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private EstadoReserva estado;
}