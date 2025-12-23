package com.plataformaeventos.web_backend.repository;

import com.plataformaeventos.web_backend.model.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EspacioRepository extends JpaRepository<Espacio, Long>, JpaSpecificationExecutor<Espacio> {
}
