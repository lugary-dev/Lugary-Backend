package com.plataformaeventos.web_backend.dto;

import com.plataformaeventos.web_backend.model.RolUsuario;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de salida para exponer información de usuarios.
 *
 * Se utiliza para evitar exponer entidades internas directamente
 * y para controlar qué campos se devuelven a los clientes.
 */
@Data
@Builder
public class UsuarioResponse {

    /**
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre de pila del usuario.
     */
    private String nombre;

    /**
     * Apellido del usuario.
     */
    private String apellido;

    /**
     * Correo electrónico registrado.
     */
    private String email;

    /**
     * Rol asignado al usuario.
     */
    private RolUsuario rol;

    /**
     * Indica si el usuario se encuentra activo.
     */
    private boolean activo;

    /**
     * Fecha y hora en la que se creó el registro de usuario.
     */
    private LocalDateTime fechaCreacion;
}