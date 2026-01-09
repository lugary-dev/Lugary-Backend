package com.plataformaeventos.web_backend.repository;

import com.plataformaeventos.web_backend.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    // Buscar pagos de un usuario ordenados por fecha (más reciente arriba)
    List<Pago> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}