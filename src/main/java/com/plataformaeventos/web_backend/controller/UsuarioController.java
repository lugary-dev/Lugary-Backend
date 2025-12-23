package com.plataformaeventos.web_backend.controller;

import com.plataformaeventos.web_backend.dto.UsuarioRegistroRequest;
import com.plataformaeventos.web_backend.dto.UsuarioResponse;
import com.plataformaeventos.web_backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST responsable de exponer operaciones relacionadas
 * con la gestión de usuarios de la plataforma.
 *
 * Endpoints actuales:
 *  - POST /api/usuarios      → registro de nuevos usuarios.
 *  - GET  /api/usuarios      → listado de usuarios.
 *
 * En etapas posteriores se ampliará con:
 *  - Actualización de datos.
 *  - Desactivación/borrado lógico.
 *  - Búsquedas específicas.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Registra un nuevo usuario en la plataforma.
     *
     * @param request datos de registro enviados por el cliente.
     * @return respuesta con el usuario creado y código HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioRegistroRequest request) {
        UsuarioResponse creado = usuarioService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Devuelve el listado completo de usuarios registrados.
     *
     * Esta operación está pensada principalmente para
     * tareas administrativas y pruebas iniciales del sistema.
     *
     * @return lista de usuarios existentes.
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        List<UsuarioResponse> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }
}