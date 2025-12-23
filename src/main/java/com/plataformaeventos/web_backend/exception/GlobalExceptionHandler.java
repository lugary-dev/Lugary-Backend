package com.plataformaeventos.web_backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Manejador global de excepciones para la API.
 *
 * Centraliza el tratamiento de errores de negocio y excepciones
 * técnicas, devolviendo respuestas HTTP consistentes y legibles.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de recursos no encontrados.
     * Devuelve código HTTP 404 (Not Found).
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleRecursoNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request
    ) {
        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Maneja errores por recursos duplicados.
     * Devuelve código HTTP 409 (Conflict).
     */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> handleRecursoDuplicado(
            RecursoDuplicadoException ex,
            HttpServletRequest request
    ) {
        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Maneja errores de validación o datos inválidos.
     * Devuelve código HTTP 400 (Bad Request).
     */
    @ExceptionHandler({
            DatosInvalidosException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiError> handleDatosInvalidos(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Maneja conflictos de negocio específicos de reservas.
     * Devuelve código HTTP 409 (Conflict).
     */
    @ExceptionHandler(ConflictoReservaException.class)
    public ResponseEntity<ApiError> handleConflictoReserva(
            ConflictoReservaException ex,
            HttpServletRequest request
    ) {
        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Maneja errores de validación generados por @Valid / @Validated
     * en los controladores (si se añaden en el futuro).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Parámetros de entrada no válidos.");

        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(mensaje)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Maneja cualquier excepción no controlada explícitamente.
     * Devuelve código HTTP 500 (Internal Server Error).
     *
     * Este método debe utilizarse como último recurso, para evitar
     * exponer detalles internos de la aplicación.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Se ha producido un error inesperado. Intente nuevamente más tarde.")
                .path(request.getRequestURI())
                .build();

        // En un entorno real, aquí debería registrarse el stacktrace en un logger.
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}