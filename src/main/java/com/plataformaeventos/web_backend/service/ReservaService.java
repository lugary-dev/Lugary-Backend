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
import com.plataformaeventos.web_backend.repository.ReservaRepository;
import com.plataformaeventos.web_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaResponse crearReserva(ReservaCrearRequest request) {

        Espacio espacio = espacioRepository.findById(request.getEspacioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El espacio especificado no existe."));

        if (espacio.getEstado() != EstadoEspacio.PUBLICADO) {
            throw new ConflictoReservaException("El espacio seleccionado no se encuentra activo.");
        }

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario especificado no existe."));

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
                .usuario(usuario)
                .fechaInicio(inicio)
                .fechaFin(fin)
                .precioTotal(precioTotal)
                .estado(EstadoReserva.CONFIRMADA)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Reserva guardada = reservaRepository.save(reserva);

        return mapearAResponse(guardada);
    }

    private void validarReglasDeNegocio(Espacio espacio, LocalDateTime inicio, LocalDateTime fin) {
        // 1. Validar aviso mínimo
        if (espacio.getAvisoMinimoHoras() != null) {
            long horasDeAnticipacion = ChronoUnit.HOURS.between(LocalDateTime.now(), inicio);
            if (horasDeAnticipacion < espacio.getAvisoMinimoHoras()) {
                throw new DatosInvalidosException("La reserva debe hacerse con al menos " + espacio.getAvisoMinimoHoras() + " horas de anticipación.");
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
        if (espacio.getAnticipacionMaximaMeses() != null) {
            LocalDateTime fechaMaxima = LocalDateTime.now().plusMonths(espacio.getAnticipacionMaximaMeses());
            if (fin.isAfter(fechaMaxima)) {
                throw new DatosInvalidosException("No se pueden hacer reservas con más de " + espacio.getAnticipacionMaximaMeses() + " meses de anticipación.");
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
                .usuarioId(reserva.getUsuario().getId())
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
                        .imagenUrlEspacio(reserva.getEspacio().getImagenUrl())
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
                        .usuarioId(reserva.getUsuario().getId())
                        .nombreUsuario(reserva.getUsuario().getNombre())
                        .emailUsuario(reserva.getUsuario().getEmail())
                        .fechaInicio(reserva.getFechaInicio())
                        .fechaFin(reserva.getFechaFin())
                        .estado(reserva.getEstado())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelarReserva(Long reservaId, Long usuarioId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no existe."));

        if (!reserva.getUsuario().getId().equals(usuarioId)) {
            throw new DatosInvalidosException("No tiene permisos para cancelar esta reserva.");
        }

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new DatosInvalidosException("La reserva ya se encuentra cancelada.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);

        reservaRepository.save(reserva);
    }
}
