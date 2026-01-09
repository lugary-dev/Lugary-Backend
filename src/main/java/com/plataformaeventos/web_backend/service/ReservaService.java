package com.plataformaeventos.web_backend.service;

import com.plataformaeventos.web_backend.dto.ReservaCrearRequest;
import com.plataformaeventos.web_backend.dto.ReservaEspacioResponse;
import com.plataformaeventos.web_backend.dto.ReservaResponse;
import com.plataformaeventos.web_backend.dto.ReservaUsuarioResponse;
import com.plataformaeventos.web_backend.exception.ConflictoReservaException;
import com.plataformaeventos.web_backend.exception.DatosInvalidosException;
import com.plataformaeventos.web_backend.exception.RecursoNoEncontradoException;
import com.plataformaeventos.web_backend.model.*;
import com.plataformaeventos.web_backend.repository.EspacioRepository;
import com.plataformaeventos.web_backend.repository.PagoRepository;
import com.plataformaeventos.web_backend.repository.ReservaRepository;
import com.plataformaeventos.web_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoRepository pagoRepository; // Inyectamos el repositorio de pagos

    @Transactional
    public ReservaResponse crearReserva(ReservaCrearRequest request) {

        Espacio espacio = espacioRepository.findById(request.getEspacioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El espacio especificado no existe."));

        if (espacio.getEstado() != EstadoEspacio.PUBLICADO) {
            throw new ConflictoReservaException("El espacio seleccionado no se encuentra activo.");
        }

        Usuario usuario = null;
        
        // Lógica para determinar Usuario vs Invitado
        if (request.getUsuarioId() != null) {
            // Caso 1: Usuario Registrado
            usuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("El usuario especificado no existe."));
        } else {
            // Caso 2: Invitado (Sin cuenta)
            // Validar que el espacio permita invitados (opcional, si tienes esa config)
            // if (!espacio.isPermiteInvitados()) throw ...

            // Validar datos de contacto obligatorios
            if (request.getEmailInvitado() == null || request.getNombreInvitado() == null) {
                throw new DatosInvalidosException("Para reservar como invitado, debe proporcionar nombre y email.");
            }
        }

        LocalDateTime inicio = request.getFechaInicio();
        LocalDateTime fin = request.getFechaFin();

        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new DatosInvalidosException("El rango de fechas de la reserva no es válido.");
        }

        // Validaciones de negocio adicionales
        validarReglasDeNegocio(espacio, inicio, fin);

        boolean haySolapamiento = existeSolapamiento(espacio, inicio, fin);
        if (haySolapamiento) {
            throw new ConflictoReservaException("El espacio no se encuentra disponible en el rango horario solicitado.");
        }

        BigDecimal precioTotal = calcularPrecioTotal(espacio, inicio, fin);

        Reserva reserva = Reserva.builder()
                .espacio(espacio)
                .usuario(usuario) // Puede ser null
                .fechaInicio(inicio)
                .fechaFin(fin)
                .precioTotal(precioTotal)
                .estado(EstadoReserva.CONFIRMADA)
                .fechaCreacion(LocalDateTime.now())
                // Datos de invitado
                .nombreInvitado(request.getNombreInvitado())
                .emailInvitado(request.getEmailInvitado())
                .telefonoInvitado(request.getTelefonoInvitado())
                .build();

        Reserva guardada = reservaRepository.save(reserva);

        // =================================================================
        // 2. NUEVA LÓGICA: SIMULAR EL PAGO AUTOMÁTICO (El "Hook")
        // =================================================================
        // Solo generamos pago si hay un usuario registrado (para historial)
        // Opcional: También podrías guardar pagos de invitados si quisieras
        if (usuario != null) {
            Pago nuevoPago = Pago.builder()
                    .monto(guardada.getPrecioTotal())
                    .concepto("Reserva - " + guardada.getEspacio().getNombre())
                    .fecha(LocalDateTime.now())
                    .metodoPago("Simulación (Saldo en cuenta)")
                    .estado(EstadoPago.APROBADO)
                    .tipo(TipoPago.PAGO)
                    .usuario(usuario)
                    .reserva(guardada)
                    .build();

            pagoRepository.save(nuevoPago);
        }
        // =================================================================

        return mapearAResponse(guardada);
    }

    private void validarReglasDeNegocio(Espacio espacio, LocalDateTime inicio, LocalDateTime fin) {
        // 1. Validar aviso mínimo
        if (espacio.getAvisoMinimo() != null) {
            long horasDeAnticipacion = ChronoUnit.HOURS.between(LocalDateTime.now(), inicio);
            if (horasDeAnticipacion < espacio.getAvisoMinimo()) {
                throw new DatosInvalidosException("La reserva debe hacerse con al menos " + espacio.getAvisoMinimo() + " horas de anticipación.");
            }
        }

        // 2. Validar estadía mínima
        if (espacio.getEstadiaMinima() != null) {
            if ("DIA".equalsIgnoreCase(espacio.getUnidadPrecio())) {
                long dias = ChronoUnit.DAYS.between(inicio.toLocalDate(), fin.toLocalDate());
                // Si es el mismo día cuenta como 1 día
                if (dias == 0) dias = 1; 
                if (dias < espacio.getEstadiaMinima()) {
                    throw new DatosInvalidosException("La estadía mínima es de " + espacio.getEstadiaMinima() + " días.");
                }
            } else if ("HORA".equalsIgnoreCase(espacio.getUnidadPrecio())) {
                long horas = ChronoUnit.HOURS.between(inicio, fin);
                if (horas < espacio.getEstadiaMinima()) {
                    throw new DatosInvalidosException("La estadía mínima es de " + espacio.getEstadiaMinima() + " horas.");
                }
            }
        }
        
        // 3. Validar anticipación máxima (meses)
        if (espacio.getAnticipacionMaxima() != null) {
            LocalDateTime fechaMaxima = LocalDateTime.now().plusMonths(espacio.getAnticipacionMaxima());
            if (fin.isAfter(fechaMaxima)) {
                throw new DatosInvalidosException("No se pueden hacer reservas con más de " + espacio.getAnticipacionMaxima() + " meses de anticipación.");
            }
        }
    }

    public List<ReservaResponse> listarPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario especificado no existe."));

        return reservaRepository.findByUsuario(usuario)
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public List<ReservaResponse> listarPorEspacio(Long espacioId) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El espacio especificado no existe."));

        return reservaRepository.findByEspacio(espacio)
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    private boolean existeSolapamiento(Espacio espacio, LocalDateTime inicio, LocalDateTime fin) {
        boolean solapeConfirmadas = !reservaRepository
                .findByEspacioAndEstadoAndFechaInicioBeforeAndFechaFinAfter(
                        espacio,
                        EstadoReserva.CONFIRMADA,
                        fin,
                        inicio
                ).isEmpty();

        boolean solapePendientes = !reservaRepository
                .findByEspacioAndEstadoAndFechaInicioBeforeAndFechaFinAfter(
                        espacio,
                        EstadoReserva.PENDIENTE,
                        fin,
                        inicio
                ).isEmpty();

        return solapeConfirmadas || solapePendientes;
    }

    private BigDecimal calcularPrecioTotal(Espacio espacio, LocalDateTime inicio, LocalDateTime fin) {
        String unidad = espacio.getUnidadPrecio().toUpperCase();

        switch (unidad) {
            case "DIA":
                long dias = ChronoUnit.DAYS.between(inicio.toLocalDate(), fin.toLocalDate());
                // Si la fecha fin es al día siguiente pero misma hora, o mayor, cuenta como día completo.
                // Ajuste simple: si es por día, cobramos por noche o por día calendario.
                // Asumiremos cobro por día calendario o bloque de 24h.
                if (dias == 0) dias = 1; 
                return espacio.getPrecio().multiply(BigDecimal.valueOf(dias)).setScale(2, RoundingMode.HALF_UP);
            case "EVENTO":
                return espacio.getPrecio().setScale(2, RoundingMode.HALF_UP);
            case "HORA":
            default:
                Duration duracion = Duration.between(inicio, fin);
                BigDecimal minutos = BigDecimal.valueOf(duracion.toMinutes());
                BigDecimal horas = minutos.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                return espacio.getPrecio().multiply(horas).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private ReservaResponse mapearAResponse(Reserva reserva) {
        return ReservaResponse.builder()
                .id(reserva.getId())
                .espacioId(reserva.getEspacio().getId())
                .usuarioId(reserva.getUsuario() != null ? reserva.getUsuario().getId() : null) // Manejo de null
                .fechaInicio(reserva.getFechaInicio())
                .fechaFin(reserva.getFechaFin())
                .precioTotal(reserva.getPrecioTotal())
                .estado(reserva.getEstado())
                .fechaCreacion(reserva.getFechaCreacion())
                .build();
    }

    public List<ReservaUsuarioResponse> obtenerReservasDeUsuario(Long usuarioId) {
        List<Reserva> reservas = reservaRepository
                .findByUsuarioIdOrderByFechaInicioDesc(usuarioId);

        return reservas.stream()
                .map(reserva -> ReservaUsuarioResponse.builder()
                        .id(reserva.getId())
                        .espacioId(reserva.getEspacio().getId())
                        .nombreEspacio(reserva.getEspacio().getNombre())
                        .direccionEspacio(reserva.getEspacio().getDireccion())
                        .imagenUrlEspacio(
                            (reserva.getEspacio().getImagenes() != null && !reserva.getEspacio().getImagenes().isEmpty())
                            ? reserva.getEspacio().getImagenes().get(0).getUrl()
                            : null
                        )
                        .fechaInicio(reserva.getFechaInicio())
                        .fechaFin(reserva.getFechaFin())
                        .estado(reserva.getEstado())
                        .precio(reserva.getEspacio().getPrecio())
                        .unidadPrecio(reserva.getEspacio().getUnidadPrecio())
                        .build())
                .collect(Collectors.toList());
    }

    public List<ReservaEspacioResponse> obtenerReservasDeEspacio(
            Long espacioId,
            Long usuarioId
    ) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El espacio no existe."));

        if (!espacio.getPropietario().getId().equals(usuarioId)) {
            throw new DatosInvalidosException("No tiene permisos para ver las reservas de este espacio.");
        }

        List<Reserva> reservas = reservaRepository.findByEspacioIdOrderByFechaInicioAsc(espacioId);

        return reservas.stream()
                .map(reserva -> ReservaEspacioResponse.builder()
                        .id(reserva.getId())
                        .usuarioId(reserva.getUsuario() != null ? reserva.getUsuario().getId() : null)
                        .nombreUsuario(reserva.getUsuario() != null ? reserva.getUsuario().getNombre() : reserva.getNombreInvitado())
                        .emailUsuario(reserva.getUsuario() != null ? reserva.getUsuario().getEmail() : reserva.getEmailInvitado())
                        .fechaInicio(reserva.getFechaInicio())
                        .fechaFin(reserva.getFechaFin())
                        .estado(reserva.getEstado())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una lista de todas las fechas (días) que están ocupados
     * por reservas CONFIRMADAS o PENDIENTES para un espacio dado.
     */
    public List<LocalDate> obtenerFechasOcupadas(Long espacioId) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El espacio no existe."));

        // Buscamos todas las reservas futuras que no estén canceladas
        List<Reserva> reservas = reservaRepository.findByEspacioAndEstadoAndFechaInicioBeforeAndFechaFinAfter(
                espacio, EstadoReserva.CONFIRMADA, LocalDateTime.now().plusYears(2), LocalDateTime.now()
        );
        // Nota: La consulta de arriba es un ejemplo simplificado. 
        // Lo ideal es buscar todas las reservas >= hoy.
        // Usaremos una estrategia más simple: traer todas las futuras y procesarlas.
        
        // Mejor aproximación con los métodos que ya tenemos o podríamos tener:
        // Vamos a buscar todas las reservas del espacio y filtrar en memoria las que nos interesan
        // para no complicar el repositorio ahora mismo.
        List<Reserva> todasLasReservas = reservaRepository.findByEspacioIdOrderByFechaInicioAsc(espacioId);
        
        List<LocalDate> fechasOcupadas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (Reserva reserva : todasLasReservas) {
            // Solo nos interesan reservas Confirmadas o Pendientes
            if (reserva.getEstado() == EstadoReserva.CANCELADA) continue;
            
            // Solo reservas futuras o actuales
            if (reserva.getFechaFin().toLocalDate().isBefore(hoy)) continue;

            LocalDate inicio = reserva.getFechaInicio().toLocalDate();
            LocalDate fin = reserva.getFechaFin().toLocalDate();

            // Agregamos todos los días del rango a la lista
            // stream().datesUntil es muy útil aquí (Java 9+)
            inicio.datesUntil(fin.plusDays(1)).forEach(fechasOcupadas::add);
        }

        return fechasOcupadas.stream().distinct().collect(Collectors.toList());
    }

    @Transactional
    public void cancelarReserva(Long reservaId, Long usuarioId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no existe."));

        if (reserva.getUsuario() != null && !reserva.getUsuario().getId().equals(usuarioId)) {
            throw new DatosInvalidosException("No tiene permisos para cancelar esta reserva.");
        }
        // TODO: Agregar lógica para que invitados puedan cancelar (quizás con un token por email)

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new DatosInvalidosException("La reserva ya se encuentra cancelada.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);

        reservaRepository.save(reserva);
    }
}