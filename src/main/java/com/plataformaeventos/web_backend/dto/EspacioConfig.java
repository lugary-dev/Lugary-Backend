package com.plataformaeventos.web_backend.dto;

import com.plataformaeventos.web_backend.model.CobroDeposito;
import com.plataformaeventos.web_backend.model.ModoReserva;
import com.plataformaeventos.web_backend.model.PoliticaCancelacion;
import com.plataformaeventos.web_backend.model.TipoReserva;
import com.plataformaeventos.web_backend.model.VisibilidadDireccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioConfig {
    private String unidadPrecio;
    
    // Ubicación
    private Double latitud;
    private Double longitud;
    private String googlePlaceId;
    private String referencia;
    
    // Precios y Depósito
    private BigDecimal precioFinDeSemana;
    private BigDecimal cargoLimpieza;
    private BigDecimal montoDeposito;
    private CobroDeposito cobroDeposito;
    
    // Reglas y Tiempos
    private LocalTime horaCheckIn;
    private LocalTime horaCheckOut;
    private Integer tiempoPreparacion;
    private Integer avisoMinimo;
    private Integer anticipacionMaxima;
    private Integer estadiaMinima;
    private List<String> diasBloqueados;
    
    // Configuración y Privacidad
    private TipoReserva tipoReserva;
    private PoliticaCancelacion politicaCancelacion;
    private VisibilidadDireccion mostrarDireccionExacta;
    private Boolean acceptUnverifiedUsers;
    private Boolean permiteEstadiaNocturna;
    private Boolean permiteReservasInvitado;
    
    // NUEVO: Modo de Reserva (Rango vs Múltiple)
    private ModoReserva modoReserva;
}