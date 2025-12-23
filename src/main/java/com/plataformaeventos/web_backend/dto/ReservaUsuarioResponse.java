package com.plataformaeventos.web_backend.dto;

import com.plataformaeventos.web_backend.model.EstadoReserva;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservaUsuarioResponse {

    private Long id;
    private Long espacioId;
    private String nombreEspacio;
    private String direccionEspacio;
    private String imagenUrlEspacio; // ¡NUEVO CAMPO!
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoReserva estado;
    private BigDecimal precio;
    private String unidadPrecio;
}
