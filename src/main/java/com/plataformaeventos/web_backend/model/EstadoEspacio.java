package com.plataformaeventos.web_backend.model;

public enum EstadoEspacio {
    PUBLICADO, // Visible en el lobby y para búsquedas
    PAUSADO,   // No visible en el lobby, solo para el propietario
    BORRADOR,  // Incompleto, solo visible para el propietario
    ELIMINADO  // Borrado lógico, no visible para nadie (futuro uso)
}
