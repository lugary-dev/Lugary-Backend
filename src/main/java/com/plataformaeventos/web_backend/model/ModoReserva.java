package com.plataformaeventos.web_backend.model;

/**
 * Define cómo se comporta el calendario de reservas para un espacio.
 */
public enum ModoReserva {
    /**
     * El usuario selecciona una fecha de inicio y una de fin.
     * Ideal para alojamientos, hoteles, alquiler vacacional.
     * Comportamiento: Rango continuo.
     */
    POR_RANGO,

    /**
     * El usuario selecciona días sueltos (ej: Lunes y Miércoles).
     * Ideal para quinchos, salones de eventos, coworking.
     * Comportamiento: Selección múltiple independiente.
     */
    POR_DIA
}