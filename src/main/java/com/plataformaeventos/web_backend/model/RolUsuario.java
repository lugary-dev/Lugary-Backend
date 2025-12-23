package com.plataformaeventos.web_backend.model;

/**
 * Enumeración de roles de usuario dentro de la plataforma.
 *
 * CLIENTE   → Usuario que realiza reservas de espacios.
 * PROPIETARIO → Usuario que publica y administra sus espacios.
 * ADMIN     → Usuario con permisos de administración global.
 */
public enum RolUsuario {
    CLIENTE,
    PROPIETARIO,
    ADMIN
}