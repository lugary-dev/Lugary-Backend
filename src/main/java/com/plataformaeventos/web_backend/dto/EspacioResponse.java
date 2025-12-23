package com.plataformaeventos.web_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EspacioResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String direccion;
    private Integer capacidadMaxima;
    private BigDecimal precio;
    // private String unidadPrecio; // Se mueve dentro de config
    private String estado;
    private Long propietarioId;
    private LocalDateTime fechaCreacion;
    private List<String> imagenes; // Reemplaza a imagenUrl
    private List<String> servicios;
    private List<String> reglas;
    
    private EspacioConfig config;
    private List<String> fechasOcupadas;
}
