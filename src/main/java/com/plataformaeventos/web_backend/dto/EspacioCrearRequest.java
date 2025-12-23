package com.plataformaeventos.web_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class EspacioCrearRequest {
    @NotBlank(message = "El nombre es obligatorio, incluso para borradores.")
    private String nombre;
    
    private String descripcion;
    private String tipo;
    private String direccion;
    private Integer capacidadMaxima;
    private BigDecimal precio;
    private String unidadPrecio;
    private Long propietarioId;
    private List<String> servicios;
    private List<String> reglas;
    private List<String> imageOrder;
    private Map<String, Double> focalPoint;
    private String estado; // "PUBLICADO" o "BORRADOR"

    // Nuevos campos de configuración opcionales en la creación
    private Integer avisoMinimoHoras;
    private Integer anticipacionMaximaMeses;
    private Integer estadiaMinima;
    private String horaCheckIn;
    private String horaCheckOut;
    private List<String> diasBloqueados;
    private Boolean permiteReservasInvitado;
}
