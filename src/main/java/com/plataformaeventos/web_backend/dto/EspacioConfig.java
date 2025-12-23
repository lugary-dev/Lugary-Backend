package com.plataformaeventos.web_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioConfig {
    private String unidadPrecio;
    private Integer avisoMinimoHoras;
    private Integer anticipacionMaximaMeses;
    private Integer estadiaMinima;
    private String horaCheckIn;
    private String horaCheckOut;
    private List<String> diasBloqueados;
    private Boolean permiteReservasInvitado;
}
