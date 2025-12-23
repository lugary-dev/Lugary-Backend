package com.plataformaeventos.web_backend.repository;

import com.plataformaeventos.web_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Usuario.
 *
 * Extiende JpaRepository para aprovechar las operaciones CRUD estándar
 * y permite declarar métodos de consulta adicionales por convención.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email correo a buscar.
     * @return Optional con el usuario si existe, vacío en caso contrario.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario registrado con el correo indicado.
     *
     * @param email correo a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existsByEmail(String email);
}