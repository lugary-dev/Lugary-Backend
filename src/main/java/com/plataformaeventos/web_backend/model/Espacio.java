package com.plataformaeventos.web_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"reservas", "imagenes"})
@Entity
@Table(name = "espacios")
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000) // Permite nulo para borrador
    private String descripcion;

    @Column(length = 100)
    private String tipo;

    @Column(length = 255) // Permite nulo para borrador
    private String direccion;

    @Column // Permite nulo para borrador
    private Integer capacidadMaxima;

    @Column(precision = 10, scale = 2) // Permite nulo para borrador
    private BigDecimal precio;

    @Column(length = 50) // Permite nulo para borrador
    private String unidadPrecio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoEspacio estado = EstadoEspacio.PUBLICADO;

    @OneToMany(mappedBy = "espacio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    @BatchSize(size = 10)
    @Builder.Default
    private List<ImagenEspacio> imagenes = new ArrayList<>();

    @Column(length = 500)
    private String servicios;

    @Column(length = 500)
    private String reglas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Usuario propietario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "espacio", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reserva> reservas;

    // Nuevos campos de configuración
    @Column(name = "aviso_minimo_horas")
    private Integer avisoMinimoHoras;

    @Column(name = "anticipacion_maxima_meses")
    private Integer anticipacionMaximaMeses;

    @Column(name = "estadia_minima")
    private Integer estadiaMinima;

    @Column(name = "hora_check_in", length = 5)
    private String horaCheckIn;

    @Column(name = "hora_check_out", length = 5)
    private String horaCheckOut;

    @Column(name = "dias_bloqueados", length = 50)
    private String diasBloqueados; // Guardado como "S,D"

    @Column(name = "permite_reservas_invitado")
    private Boolean permiteReservasInvitado;

    @Transient
    public String getImagenUrl() {
        if (imagenes == null || imagenes.isEmpty()) {
            return null;
        }
        return imagenes.get(0).getUrl();
    }
}
