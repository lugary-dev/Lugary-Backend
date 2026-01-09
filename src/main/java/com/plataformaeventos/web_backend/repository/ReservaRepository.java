package com.plataformaeventos.web_backend.repository;

import com.plataformaeventos.web_backend.model.EstadoReserva;
import com.plataformaeventos.web_backend.model.Reserva;
import com.plataformaeventos.web_backend.model.Usuario;
import com.plataformaeventos.web_backend.model.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Reserva.
 */
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Obtiene las reservas realizadas por un usuario dado.
     *
     * @param usuario usuario que realizó las reservas.
     * @return lista de reservas asociadas al usuario.
     */
    List<Reserva> findByUsuario(Usuario usuario);

    /**
     * Obtiene las reservas asociadas a un espacio concreto.
     *
     * @param espacio espacio reservado.
     * @return lista de reservas del espacio.
     */
    List<Reserva> findByEspacio(Espacio espacio);

    /**
     * Busca reservas de un espacio que se solapan con un rango horario dado
     * y que no estén canceladas.
     *
     * Útil para validar disponibilidad antes de confirmar una nueva reserva.
     *
     * @param espacio      espacio a consultar.
     * @param inicio       fecha/hora de inicio del rango.
     * @param fin          fecha/hora de fin del rango.
     * @param estado       estado a filtrar (por ejemplo, CONFIRMADA o PENDIENTE).
     * @return lista de reservas que se solapan en el rango indicado.
     */
    List<Reserva> findByEspacioAndEstadoAndFechaInicioBeforeAndFechaFinAfter(
            Espacio espacio,
            EstadoReserva estado,
            LocalDateTime fin,
            LocalDateTime inicio
    );

    /**
     * Obtiene las reservas realizadas por un usuario,
     * ordenadas de más reciente a más antigua.
     * 
     * OPTIMIZACIÓN: Usamos JOIN FETCH para traer los datos del Espacio en la misma consulta
     * y evitar el problema N+1.
     */
    @Query("SELECT r FROM Reserva r JOIN FETCH r.espacio WHERE r.usuario.id = :usuarioId ORDER BY r.fechaInicio DESC")
    List<Reserva> findByUsuarioIdOrderByFechaInicioDesc(@Param("usuarioId") Long usuarioId);

    boolean existsByEspacioIdAndEstadoNot(Long espacioId, EstadoReserva estado);

    /**
     * Reservas asociadas a un espacio, ordenadas por fecha de inicio.
     */
    List<Reserva> findByEspacioIdOrderByFechaInicioAsc(Long espacioId);

    /**
     * 👇 NUEVO: todas las reservas de un espacio, sin orden estricto
     */
    List<Reserva> findByEspacioId(Long espacioId);

    List<Reserva> findByEspacioAndEstadoNot(Espacio espacio, EstadoReserva estado);
}