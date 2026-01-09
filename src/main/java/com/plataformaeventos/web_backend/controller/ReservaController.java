package com.plataformaeventos.web_backend.controller;

import com.plataformaeventos.web_backend.dto.ReservaCrearRequest;
import com.plataformaeventos.web_backend.dto.ReservaEspacioResponse;
import com.plataformaeventos.web_backend.dto.ReservaResponse;
import com.plataformaeventos.web_backend.dto.ReservaUsuarioResponse;
import com.plataformaeventos.web_backend.model.Usuario;
import com.plataformaeventos.web_backend.repository.UsuarioRepository;
import com.plataformaeventos.web_backend.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST responsable de exponer operaciones relacionadas
 * con la gestión de reservas.
 *
 * Endpoints actuales:
 *  - POST /api/reservas                 → creación de una reserva.
 *  - GET  /api/reservas/usuario/{id}    → reservas de un usuario.
 *  - GET  /api/reservas/espacio/{id}    → reservas de un espacio.
 *  - GET  /api/reservas/ocupadas/{id}   → fechas ocupadas de un espacio.
 */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Crea una nueva reserva para un espacio determinado.
     *
     * @param request datos de creación de la reserva.
     * @return reserva creada con código HTTP 201.
     */
    @PostMapping
    public ResponseEntity<ReservaResponse> crear(
            @RequestBody ReservaCrearRequest request,
            Authentication authentication
    ) {
        // Lógica para determinar si es usuario registrado o invitado
        if (authentication != null && authentication.isAuthenticated() && 
           !authentication.getPrincipal().equals("anonymousUser")) {
            
            String email = authentication.getName(); 
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario logueado no encontrado en BD"));
            
            // Inyectamos el ID del usuario autenticado en el request
            request.setUsuarioId(usuario.getId());
        }
        
        // Si no hay usuario autenticado, request.getUsuarioId() vendrá null
        // y el servicio deberá manejar la lógica de invitado.

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

    /**
     * Devuelve la lista de fechas ocupadas para un espacio.
     * Útil para bloquear días en el calendario del frontend.
     */
    @GetMapping("/ocupadas/{espacioId}")
    public List<LocalDate> obtenerFechasOcupadas(@PathVariable Long espacioId) {
        return reservaService.obtenerFechasOcupadas(espacioId);
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