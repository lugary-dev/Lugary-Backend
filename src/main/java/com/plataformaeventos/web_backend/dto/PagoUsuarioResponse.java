package com.plataformaeventos.web_backend.dto;

import com.plataformaeventos.web_backend.model.EstadoPago;
import com.plataformaeventos.web_backend.model.TipoPago;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PagoUsuarioResponse {
    private String id;        // Frontend espera "TRX-123"
    private String fecha;     // ISO String
    private String concepto;
    private BigDecimal monto;
    private String metodo;
    private EstadoPago estado;
    private TipoPago tipo;
}