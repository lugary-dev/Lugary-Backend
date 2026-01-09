package com.plataformaeventos.web_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa a un usuario de la plataforma.
 *
 * Un usuario puede:
 *  - Reservar espacios (rol CLIENTE).
 *  - Publicar espacios (rol PROPIETARIO).
 *  - Administrar el sistema (rol ADMIN).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"espaciosPropios", "reservas"})
@Entity
@Table(name = "usuarios")
public class Usuario {

    /**
     * Identificador único del usuario en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de pila del usuario.
     */
    @Column(nullable = false, length = 100)
    private String nombre;

    /**
     * Apellido del usuario.
     */
    @Column(nullable = false, length = 100)
    private String apellido;

    /**
     * Correo electrónico único utilizado para autenticación y contacto.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Número de teléfono (Formato E.164 recomendado: +549...)
     */
    @Column(length = 20)
    private String telefono;

    /**
     * Hash de la contraseña del usuario.
     *
     * Importante:
     *  - Nunca almacenar la contraseña en texto plano.
     *  - Debe ser generada mediante un algoritmo de hash seguro (BCrypt, Argon2, etc.).
     */
    @Column(nullable = false, length = 200)
    private String passwordHash;

    /**
     * Rol del usuario dentro de la plataforma.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    /**
     * Indica si el usuario se encuentra activo.
     * Permite deshabilitar cuentas sin eliminarlas físicamente.
     */
    @Column(nullable = false)
    private boolean activo;

    /**
     * Indica si es la primera vez que el usuario inicia sesión (o si no ha visto el modal de bienvenida).
     * true = debe ver el modal.
     * false = ya vio el modal.
     */
    @Column(name = "is_first_login", nullable = false)
    @Builder.Default
    private boolean firstLogin = true;

    /**
     * URL de la imagen de perfil alojada en Cloudinary.
     */
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    /**
     * Fecha y hora de creación del registro de usuario.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Espacios publicados por el usuario cuando actúa como PROPIETARIO.
     */
    @OneToMany(mappedBy = "propietario", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Espacio> espaciosPropios;

    /**
     * Reservas realizadas por el usuario cuando actúa como CLIENTE.
     */
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Reserva> reservas;
}