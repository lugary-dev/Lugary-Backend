package com.plataformaeventos.web_backend.service;

import com.plataformaeventos.web_backend.dto.UsuarioRegistroRequest;
import com.plataformaeventos.web_backend.dto.UsuarioResponse;
import com.plataformaeventos.web_backend.dto.UsuarioUpdateRequest;
import com.plataformaeventos.web_backend.exception.RecursoDuplicadoException;
import com.plataformaeventos.web_backend.exception.RecursoNoEncontradoException;
import com.plataformaeventos.web_backend.model.RolUsuario;
import com.plataformaeventos.web_backend.model.Usuario;
import com.plataformaeventos.web_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de usuarios.
 *
 * Encapsula la lógica de negocio relacionada con:
 *  - Registro de nuevos usuarios.
 *  - Consulta de usuarios existentes.
 *
 * Esta capa actúa como intermediaria entre los controladores REST
 * y la capa de acceso a datos (repositorios).
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    /**
     * Registra un nuevo usuario en la plataforma.
     *
     * Reglas actuales:
     *  - El email no debe estar ya registrado.
     *  - La contraseña se almacena en formato hash usando BCrypt.
     *  - Por defecto se asigna el rol CLIENTE y estado activo = true.
     *
     * @param request DTO con los datos de registro enviados por el cliente.
     * @return DTO con los datos del usuario creado.
     * @throws RecursoDuplicadoException si el email ya está en uso.
     */
    public UsuarioResponse registrarUsuario(UsuarioRegistroRequest request) {

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RecursoDuplicadoException("El correo electrónico ya se encuentra registrado.");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(RolUsuario.CLIENTE)          // Por defecto, todos los que se registran son CLIENTE
                .activo(true)                     // Usuario habilitado al crearse
                .firstLogin(true)                 // Por defecto es su primer login
                .fechaCreacion(LocalDateTime.now())
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        return mapearAResponse(guardado);
    }

    /**
     * Obtiene el listado completo de usuarios registrados.
     *
     * Este método es útil en etapas iniciales de desarrollo
     * y para tareas administrativas básicas.
     *
     * @return lista de DTOs representando a los usuarios existentes.
     */
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los datos de un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return DTO con los datos del usuario.
     * @throws RecursoNoEncontradoException si el usuario no existe.
     */
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));
        return mapearAResponse(usuario);
    }

    /**
     * Actualiza los datos personales de un usuario.
     *
     * @param id ID del usuario a actualizar.
     * @param request DTO con los nuevos datos (nombre, apellido, teléfono).
     * @return DTO con los datos actualizados.
     */
    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, UsuarioUpdateRequest request) {
        // 1. Buscamos al usuario
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));

        // 2. Actualizamos sus datos
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        
        // Actualizamos el teléfono si viene en la petición
        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            usuario.setTelefono(request.getTelefono());
        }

        // 3. Guardamos cambios
        Usuario actualizado = usuarioRepository.save(usuario);

        // 4. Devolvemos la respuesta actualizada
        return mapearAResponse(actualizado);
    }

    /**
     * Actualiza la foto de perfil del usuario.
     *
     * @param id ID del usuario.
     * @param file Archivo de imagen recibido.
     * @return DTO con los datos actualizados (incluyendo la nueva URL).
     */
    @Transactional
    public UsuarioResponse actualizarFotoPerfil(Long id, MultipartFile file) throws IOException {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // 1. Subir a Cloudinary
        String url = cloudinaryService.subirImagen(file);

        // 2. Guardar URL en BD
        usuario.setImagenUrl(url);
        Usuario guardado = usuarioRepository.save(usuario);

        return mapearAResponse(guardado);
    }

    /**
     * Actualiza el flag de firstLogin a false para un usuario dado.
     *
     * @param usuarioId ID del usuario.
     */
    @Transactional
    public void completarBienvenida(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId));
        
        usuario.setFirstLogin(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Mapea una entidad Usuario a su DTO de salida.
     *
     * Este método centraliza la transformación para evitar
     * duplicar lógica de mapeo en distintos puntos del código.
     *
     * @param usuario entidad persistida.
     * @return DTO de salida listo para ser expuesto vía API.
     */
    private UsuarioResponse mapearAResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .firstLogin(usuario.isFirstLogin())
                .imagenUrl(usuario.getImagenUrl()) // Mapeamos la URL de la imagen
                .telefono(usuario.getTelefono()) // Mapeamos el teléfono
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }
}