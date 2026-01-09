package com.plataformaeventos.web_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 100)
    private String tipo;

    @Column(length = 255)
    private String direccion;

    @Column
    private Integer capacidadMaxima;

    @Column(precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(length = 50)
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

    // --- Nuevos campos de Ubicación ---
    // Se eliminan precision y scale porque generan error con Double en Hibernate
    // "scale has no meaning for SQL floating point types"
    @Column
    private Double latitud;

    @Column
    private Double longitud;

    @Column(name = "google_place_id")
    private String googlePlaceId;

    @Column(columnDefinition = "TEXT")
    private String referencia;

    // --- Precios y Depósito ---
    @Column(name = "precio_fin_de_semana", precision = 10, scale = 2)
    private BigDecimal precioFinDeSemana;

    @Column(name = "cargo_limpieza", precision = 10, scale = 2)
    private BigDecimal cargoLimpieza;

    @Column(name = "monto_deposito", precision = 10, scale = 2)
    private BigDecimal montoDeposito;

    @Enumerated(EnumType.STRING)
    @Column(name = "cobro_deposito")
    private CobroDeposito cobroDeposito;

    // --- Reglas y Tiempos ---
    @Column(name = "hora_check_in")
    private LocalTime horaCheckIn;

    @Column(name = "hora_check_out")
    private LocalTime horaCheckOut;

    @Column(name = "tiempo_preparacion")
    private Integer tiempoPreparacion; // minutos

    @Column(name = "aviso_minimo")
    private Integer avisoMinimo; // horas

    @Column(name = "anticipacion_maxima")
    private Integer anticipacionMaxima; // meses

    @Column(name = "estadia_minima")
    private Integer estadiaMinima;

    @ElementCollection
    @CollectionTable(name = "espacio_dias_bloqueados", joinColumns = @JoinColumn(name = "espacio_id"))
    @Column(name = "dia_bloqueado")
    private List<String> diasBloqueados;

    // --- Configuración y Privacidad ---
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reserva")
    private TipoReserva tipoReserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "politica_cancelacion")
    private PoliticaCancelacion politicaCancelacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "mostrar_direccion_exacta")
    private VisibilidadDireccion mostrarDireccionExacta;

    @Column(name = "accept_unverified_users")
    private Boolean acceptUnverifiedUsers;

    @Column(name = "permite_estadia_nocturna")
    private Boolean permiteEstadiaNocturna;
    
    @Column(name = "permite_reservas_invitado")
    private Boolean permiteReservasInvitado;

    // --- NUEVO CAMPO: MODO DE RESERVA ---
    @Enumerated(EnumType.STRING)
    @Column(name = "modo_reserva")
    private ModoReserva modoReserva;

    @Transient
    public String getImagenUrl() {
        if (imagenes == null || imagenes.isEmpty()) {
            return null;
        }
        return imagenes.get(0).getUrl();
    }
    
    // Manual Getters to ensure visibility if Lombok fails partially
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public String getGooglePlaceId() { return googlePlaceId; }
    public String getReferencia() { return referencia; }
    public BigDecimal getPrecioFinDeSemana() { return precioFinDeSemana; }
    public BigDecimal getCargoLimpieza() { return cargoLimpieza; }
    public BigDecimal getMontoDeposito() { return montoDeposito; }
    public CobroDeposito getCobroDeposito() { return cobroDeposito; }
    public LocalTime getHoraCheckIn() { return horaCheckIn; }
    public LocalTime getHoraCheckOut() { return horaCheckOut; }
    public Integer getTiempoPreparacion() { return tiempoPreparacion; }
    public Integer getAvisoMinimo() { return avisoMinimo; }
    public Integer getAnticipacionMaxima() { return anticipacionMaxima; }
    public Integer getEstadiaMinima() { return estadiaMinima; }
    public List<String> getDiasBloqueados() { return diasBloqueados; }
    public TipoReserva getTipoReserva() { return tipoReserva; }
    public PoliticaCancelacion getPoliticaCancelacion() { return politicaCancelacion; }
    public VisibilidadDireccion getMostrarDireccionExacta() { return mostrarDireccionExacta; }
    public Boolean getAcceptUnverifiedUsers() { return acceptUnverifiedUsers; }
    public Boolean getPermiteEstadiaNocturna() { return permiteEstadiaNocturna; }
    public Boolean getPermiteReservasInvitado() { return permiteReservasInvitado; }
    public ModoReserva getModoReserva() { return modoReserva; }

    // Manual Setters
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public void setGooglePlaceId(String googlePlaceId) { this.googlePlaceId = googlePlaceId; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public void setPrecioFinDeSemana(BigDecimal precioFinDeSemana) { this.precioFinDeSemana = precioFinDeSemana; }
    public void setCargoLimpieza(BigDecimal cargoLimpieza) { this.cargoLimpieza = cargoLimpieza; }
    public void setMontoDeposito(BigDecimal montoDeposito) { this.montoDeposito = montoDeposito; }
    public void setCobroDeposito(CobroDeposito cobroDeposito) { this.cobroDeposito = cobroDeposito; }
    public void setHoraCheckIn(LocalTime horaCheckIn) { this.horaCheckIn = horaCheckIn; }
    public void setHoraCheckOut(LocalTime horaCheckOut) { this.horaCheckOut = horaCheckOut; }
    public void setTiempoPreparacion(Integer tiempoPreparacion) { this.tiempoPreparacion = tiempoPreparacion; }
    public void setAvisoMinimo(Integer avisoMinimo) { this.avisoMinimo = avisoMinimo; }
    public void setAnticipacionMaxima(Integer anticipacionMaxima) { this.anticipacionMaxima = anticipacionMaxima; }
    public void setEstadiaMinima(Integer estadiaMinima) { this.estadiaMinima = estadiaMinima; }
    public void setDiasBloqueados(List<String> diasBloqueados) { this.diasBloqueados = diasBloqueados; }
    public void setTipoReserva(TipoReserva tipoReserva) { this.tipoReserva = tipoReserva; }
    public void setPoliticaCancelacion(PoliticaCancelacion politicaCancelacion) { this.politicaCancelacion = politicaCancelacion; }
    public void setMostrarDireccionExacta(VisibilidadDireccion mostrarDireccionExacta) { this.mostrarDireccionExacta = mostrarDireccionExacta; }
    public void setAcceptUnverifiedUsers(Boolean acceptUnverifiedUsers) { this.acceptUnverifiedUsers = acceptUnverifiedUsers; }
    public void setPermiteEstadiaNocturna(Boolean permiteEstadiaNocturna) { this.permiteEstadiaNocturna = permiteEstadiaNocturna; }
    public void setPermiteReservasInvitado(Boolean permiteReservasInvitado) { this.permiteReservasInvitado = permiteReservasInvitado; }
    public void setModoReserva(ModoReserva modoReserva) { this.modoReserva = modoReserva; }
}