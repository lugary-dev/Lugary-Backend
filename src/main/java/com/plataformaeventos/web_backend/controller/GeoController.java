package com.plataformaeventos.web_backend.controller;

import com.plataformaeventos.web_backend.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;

    @GetMapping("/provincias")
    public ResponseEntity<List<String>> listarProvincias() {
        return ResponseEntity.ok(geoService.obtenerProvincias());
    }

    @GetMapping("/provincias/{nombre}/ciudades")
    public ResponseEntity<List<String>> listarCiudades(@PathVariable String nombre) {
        return ResponseEntity.ok(geoService.obtenerCiudadesPorProvincia(nombre));
    }
}
