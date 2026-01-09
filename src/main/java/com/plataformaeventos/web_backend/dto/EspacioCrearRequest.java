package com.plataformaeventos.web_backend.dto;

import com.plataformaeventos.web_backend.model.CobroDeposito;
import com.plataformaeventos.web_backend.model.ModoReserva;
import com.plataformaeventos.web_backend.model.PoliticaCancelacion;
import com.plataformaeventos.web_backend.model.TipoReserva;
import com.plataformaeventos.web_backend.model.VisibilidadDireccion;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
public class EspacioCrearRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ\\s\\.,\\-']+$", message = "El nombre contiene caracteres inválidos")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @Size(max = 100, message = "El tipo no puede exceder 100 caracteres")
    private String tipo;

    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String direccion;

    @Min(value = 1, message = "La capacidad máxima debe ser al menos 1")
    private Integer capacidadMaxima;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal precio;

    @Size(max = 50, message = "La unidad de precio no puede exceder 50 caracteres")
    private String unidadPrecio;

    private List<String> servicios;
    private List<String> reglas;
    
    // Orden de las imágenes (nombres de archivo o índices)
    private List<String> imageOrder;
    
    private Map<String, Double> focalPoint;
    
    private String estado; // "PUBLICADO" o "BORRADOR"

    // --- Ubicación ---
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "Latitud inválida")
    @DecimalMax(value = "90.0", message = "Latitud inválida")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "Longitud inválida")
    @DecimalMax(value = "180.0", message = "Longitud inválida")
    private Double longitud;

    private String googlePlaceId;

    private String referencia;

    // --- Precios y Depósito ---
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de fin de semana no puede ser negativo")
    private BigDecimal precioFinDeSemana;

    @DecimalMin(value = "0.0", inclusive = true, message = "El cargo de limpieza no puede ser negativo")
    private BigDecimal cargoLimpieza;

    @DecimalMin(value = "0.0", inclusive = true, message = "El monto de depósito no puede ser negativo")
    private BigDecimal montoDeposito;

    private CobroDeposito cobroDeposito;

    // --- Reglas y Tiempos ---
    @NotNull(message = "La hora de Check-In es obligatoria")
    private LocalTime horaCheckIn;

    @NotNull(message = "La hora de Check-Out es obligatoria")
    private LocalTime horaCheckOut;

    @Min(value = 0, message = "El tiempo de preparación no puede ser negativo")
    private Integer tiempoPreparacion;

    @Min(value = 0, message = "El aviso mínimo no puede ser negativo")
    private Integer avisoMinimo;

    @Min(value = 0, message = "La anticipación máxima no puede ser negativa")
    private Integer anticipacionMaxima;

    @Min(value = 1, message = "La estadía mínima debe ser al menos 1")
    private Integer estadiaMinima;

    private List<String> diasBloqueados;

    // --- Configuración y Privacidad ---
    @NotNull(message = "El tipo de reserva es obligatorio")
    private TipoReserva tipoReserva;

    @NotNull(message = "La política de cancelación es obligatoria")
    private PoliticaCancelacion politicaCancelacion;

    @NotNull(message = "La visibilidad de la dirección es obligatoria")
    private VisibilidadDireccion mostrarDireccionExacta;

    private Boolean acceptUnverifiedUsers;

    private Boolean permiteEstadiaNocturna;
    
    private Boolean permiteReservasInvitado;
    
    // NUEVO: Modo de Reserva
    private ModoReserva modoReserva;
}