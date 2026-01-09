package com.plataformaeventos.web_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal monto;
    
    private String concepto; // Ej: "Seña - Quincho Los Pinos"
    
    private String metodoPago; // Ej: "Visa terminada en 4242"

    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado;

    @Enumerated(EnumType.STRING)
    private TipoPago tipo;

    // Relación con Usuario (Quién pagó)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Relación con Reserva (Opcional, por si es un pago suelto)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;
}