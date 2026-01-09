package com.plataformaeventos.web_backend.service;

import com.plataformaeventos.web_backend.dto.PagoUsuarioResponse;
import com.plataformaeventos.web_backend.model.Pago;
import com.plataformaeventos.web_backend.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public List<PagoUsuarioResponse> obtenerPagosDeUsuario(Long usuarioId) {
        return pagoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PagoUsuarioResponse mapToDto(Pago pago) {
        return PagoUsuarioResponse.builder()
                .id("TRX-" + pago.getId()) // Formateamos el ID para que se vea pro
                .fecha(pago.getFecha().toString())
                .concepto(pago.getConcepto())
                .monto(pago.getMonto())
                .metodo(pago.getMetodoPago())
                .estado(pago.getEstado())
                .tipo(pago.getTipo())
                .build();
    }
}