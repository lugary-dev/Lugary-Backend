package com.plataformaeventos.web_backend.controller;

import com.plataformaeventos.web_backend.dto.UsuarioRegistroRequest;
import com.plataformaeventos.web_backend.dto.UsuarioResponse;
import com.plataformaeventos.web_backend.dto.UsuarioUpdateRequest;
import com.plataformaeventos.web_backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

/**
 * Controlador REST responsable de exponer operaciones relacionadas
 * con la gestión de usuarios de la plataforma.
 *
 * Endpoints actuales:
 * - POST /api/usuarios      → registro de nuevos usuarios.
 * - GET  /api/usuarios      → listado de usuarios.
 * - GET  /api/usuarios/{id} → obtener usuario por ID.
 * - PUT  /api/usuarios/{id} → actualizar datos del usuario (PROTEGIDO).
 * - POST /api/usuarios/{id}/imagen → subir foto de perfil (PROTEGIDO).
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Registra un nuevo usuario en la plataforma.
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioRegistroRequest request) {
        UsuarioResponse creado = usuarioService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Devuelve el listado completo de usuarios registrados.
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        List<UsuarioResponse> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene los datos de un usuario específico por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Actualiza los datos personales de un usuario.
     * 🛡️ SEGURIDAD: Verifica que el usuario logueado sea el dueño de la cuenta.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails // 1. Inyectamos al usuario logueado
    ) {
        // 2. Validamos permisos
        validarPropiedadDeCuenta(id, userDetails);

        UsuarioResponse actualizado = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Sube o actualiza la foto de perfil del usuario.
     * 🛡️ SEGURIDAD: Verifica que el usuario logueado sea el dueño de la cuenta.
     */
    @PostMapping("/{id}/imagen")
    public ResponseEntity<UsuarioResponse> subirImagen(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails // 1. Inyectamos al usuario logueado
    ) throws IOException {
        // 2. Validamos permisos
        validarPropiedadDeCuenta(id, userDetails);

        UsuarioResponse respuesta = usuarioService.actualizarFotoPerfil(id, file);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Marca que el usuario ya ha visto el modal de bienvenida.
     */
    @PatchMapping("/{id}/bienvenida")
    public ResponseEntity<Void> completarBienvenida(@PathVariable Long id) {
        usuarioService.completarBienvenida(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // MÉTODOS PRIVADOS DE SEGURIDAD
    // -------------------------------------------------------------------------

    /**
     * Verifica que el usuario autenticado (del Token) sea el mismo que el usuario
     * que se intenta modificar (del ID en la URL).
     *
     * Si no coinciden, lanza una excepción 403 Forbidden.
     */
    private void validarPropiedadDeCuenta(Long idObjetivo, UserDetails usuarioAutenticado) {
        // Obtenemos el usuario que se quiere editar desde la base de datos
        UsuarioResponse usuarioObjetivo = usuarioService.obtenerPorId(idObjetivo);

        // Obtenemos el email del token (quien hace la petición)
        String emailAutenticado = usuarioAutenticado.getUsername();

        // Comparamos los emails
        // NOTA: Si en el futuro tienes roles ADMIN, podrías agregar aquí un "|| isAdmin"
        if (!usuarioObjetivo.getEmail().equals(emailAutenticado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar este perfil.");
        }
    }
}