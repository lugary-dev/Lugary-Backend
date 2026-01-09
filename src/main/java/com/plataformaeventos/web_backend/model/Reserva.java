package com.plataformaeventos.web_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una reserva de un espacio
 * para un rango de fecha y hora determinados.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "reservas")
public class Reserva {

    /**
     * Identificador único de la reserva.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Espacio reservado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;

    /**
     * Usuario que realiza la reserva.
     * Ahora es OPCIONAL para permitir reservas de invitados.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;

    /**
     * Fecha y hora de inicio de la reserva.
     */
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    /**
     * Fecha y hora de fin de la reserva.
     */
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    /**
     * Precio total calculado para la reserva.
     * En general: (horas reservadas) × (precioPorHora del espacio).
     */
    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    /**
     * Estado actual de la reserva (pendiente, confirmada, cancelada, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    /**
     * Timestamp de creación de la reserva.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    // --- CAMPOS PARA INVITADOS (SIN CUENTA) ---
    private String nombreInvitado;
    private String emailInvitado;
    private String telefonoInvitado;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}