package com.plataformaeventos.web_backend.controller;

import com.plataformaeventos.web_backend.dto.PagoUsuarioResponse;
import com.plataformaeventos.web_backend.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PagoUsuarioResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pagoService.obtenerPagosDeUsuario(usuarioId));
    }
}