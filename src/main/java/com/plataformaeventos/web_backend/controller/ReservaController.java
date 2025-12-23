package com.plataformaeventos.web_backend.controller;

import com.plataformaeventos.web_backend.dto.ReservaCrearRequest;
import com.plataformaeventos.web_backend.dto.ReservaEspacioResponse;
import com.plataformaeventos.web_backend.dto.ReservaResponse;
import com.plataformaeventos.web_backend.dto.ReservaUsuarioResponse;
import com.plataformaeventos.web_backend.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST responsable de exponer operaciones relacionadas
 * con la gestión de reservas.
 *
 * Endpoints actuales:
 *  - POST /api/reservas                 → creación de una reserva.
 *  - GET  /api/reservas/usuario/{id}    → reservas de un usuario.
 *  - GET  /api/reservas/espacio/{id}    → reservas de un espacio.
 */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    /**
     * Crea una nueva reserva para un espacio determinado.
     *
     * @param request datos de creación de la reserva.
     * @return reserva creada con código HTTP 201.
     */
    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@RequestBody ReservaCrearRequest request) {
        ReservaResponse creada = reservaService.crearReserva(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Obtiene todas las reservas realizadas por un usuario.
     *
     * @param usuarioId identificador del usuario.
     * @return lista de reservas asociadas al usuario.
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<ReservaUsuarioResponse> obtenerReservasDeUsuario(
            @PathVariable Long usuarioId) {
        return reservaService.obtenerReservasDeUsuario(usuarioId);
    }

    /**
     * Devuelve las reservas de un espacio, únicamente si el usuario solicitante
     * es el propietario de dicho espacio.
     */
    @GetMapping("/espacio/{espacioId}")
    public List<ReservaEspacioResponse> obtenerReservasDeEspacio(
            @PathVariable Long espacioId,
            @RequestParam Long usuarioId
    ) {
        return reservaService.obtenerReservasDeEspacio(espacioId, usuarioId);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarReserva(
            @PathVariable Long id,
            @RequestParam Long usuarioId
    ) {
        reservaService.cancelarReserva(id, usuarioId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}