package com.plataformaeventos.web_backend.service;

import com.plataformaeventos.web_backend.dto.geo.GeorefResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeoService {

    // Cache en Memoria: Mapa de "Nombre Provincia" -> "Lista de Ciudades"
    private Map<String, List<String>> cacheProvincias = new HashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void inicializarDatos() {
        System.out.println("🌍 Iniciando ingesta de datos Georef (Argentina)...");
        try {
            // Pedimos TODAS las localidades de una vez (max=5000 cubre toda Argentina)
            String url = "https://apis.datos.gob.ar/georef/api/localidades?max=5000&campos=id,nombre,provincia.nombre";
            GeorefResponse response = restTemplate.getForObject(url, GeorefResponse.class);

            if (response != null && response.getLocalidades() != null) {
                // Agrupamos por nombre de provincia en un mapa temporal para evitar problemas de inferencia de tipos
                Map<String, List<String>> provinciasAgrupadas = response.getLocalidades().stream()
                        .filter(loc -> loc.getProvincia() != null && loc.getProvincia().getNombre() != null) // Filtro de seguridad
                        .collect(Collectors.groupingBy(
                                loc -> loc.getProvincia().getNombre(), // Clave: Provincia
                                TreeMap::new, // Ordenamos provincias alfabéticamente
                                Collectors.mapping(GeorefResponse.Localidad::getNombre, Collectors.toList()) // Valor: Lista de nombres
                        ));
                
                // Ordenamos las ciudades dentro de cada provincia
                provinciasAgrupadas.forEach((k, v) -> Collections.sort(v));
                
                // Asignamos el mapa ya procesado a nuestro cache
                this.cacheProvincias = provinciasAgrupadas;
                
                System.out.println("✅ Datos Georef cargados: " + this.cacheProvincias.size() + " provincias procesadas.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error conectando con Georef: " + e.getMessage());
            // Aquí podrías cargar un JSON local de respaldo si falla internet
        }
    }

    public List<String> obtenerProvincias() {
        return new ArrayList<>(cacheProvincias.keySet());
    }

    public List<String> obtenerCiudadesPorProvincia(String provincia) {
        return cacheProvincias.getOrDefault(provincia, List.of());
    }
}
