package com.plataformaeventos.web_backend.controller;

import com.plataformaeventos.web_backend.dto.ai.AiDescriptionRequest;
import com.plataformaeventos.web_backend.dto.ai.AiDescriptionResponse;
import com.plataformaeventos.web_backend.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final GeminiService geminiService;

    @PostMapping("/generate-description")
    public ResponseEntity<?> generar(@RequestBody AiDescriptionRequest request) {
        try {
            AiDescriptionResponse respuesta = geminiService.generarDescripcion(request.getDraftText());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            e.printStackTrace(); 
            Map<String, String> errorResponse = Map.of(
                "message", "Lo sentimos, la IA no está disponible en este momento.",
                "error_detail", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }
}
