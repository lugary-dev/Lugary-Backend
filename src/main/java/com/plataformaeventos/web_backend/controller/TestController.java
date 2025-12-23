package com.plataformaeventos.web_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de prueba para verificar el manejo global
 * de errores ante excepciones no controladas.
 *
 * Este controlador no forma parte del dominio de negocio
 * y puede eliminarse más adelante. Su único propósito es
 * forzar un error interno del servidor.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * Endpoint que lanza intencionalmente una RuntimeException
     * para comprobar la respuesta HTTP 500 generada por el
     * manejador global de excepciones.
     *
     * @return nunca retorna una respuesta exitosa.
     */
    @GetMapping("/error")
    public ResponseEntity<Void> lanzarError() {
        throw new RuntimeException("Error simulado para pruebas.");
    }
}
