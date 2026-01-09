package com.plataformaeventos.web_backend.repository;

import com.plataformaeventos.web_backend.model.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EspacioRepository extends JpaRepository<Espacio, Long>, JpaSpecificationExecutor<Espacio> {

    // Consulta geoespacial usando Haversine formula
    // 6371 es el radio de la tierra en km
    @Query(value = "SELECT e FROM Espacio e WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(e.latitud)) * cos(radians(e.longitud) - radians(:lon)) + sin(radians(:lat)) * sin(radians(e.latitud)))) < :distanciaKm")
    List<Espacio> findCercanos(@Param("lat") double latitud, @Param("lon") double longitud, @Param("distanciaKm") double distanciaKm);
}
