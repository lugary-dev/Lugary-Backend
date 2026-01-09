package com.plataformaeventos.web_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UsuarioUpdateRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    // Validación básica: permite +, espacios y números.
    @Pattern(regexp = "^[+]?[0-9\\s]*$", message = "El teléfono contiene caracteres inválidos")
    private String telefono;
}