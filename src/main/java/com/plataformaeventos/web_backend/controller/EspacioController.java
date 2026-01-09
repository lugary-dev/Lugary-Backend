package com.plataformaeventos.web_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataformaeventos.web_backend.config.CustomUserDetails;
import com.plataformaeventos.web_backend.dto.EspacioActualizarRequest;
import com.plataformaeventos.web_backend.dto.EspacioCrearRequest;
import com.plataformaeventos.web_backend.dto.EspacioResponse;
import com.plataformaeventos.web_backend.service.EspacioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
@Validated
public class EspacioController {

    private final EspacioService espacioService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EspacioResponse> crearEspacio(
            @RequestPart("espacio") String espacioJson,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes
    ) {
        try {
            Long usuarioId = getAuthenticatedUserId();
            EspacioCrearRequest request = objectMapper.readValue(espacioJson, EspacioCrearRequest.class);
            // Validar manualmente si es necesario o confiar en la validación del servicio si se pasa @Valid
            // Aquí pasamos el ID del usuario autenticado al servicio
            EspacioResponse nuevoEspacio = espacioService.crearEspacio(request, imagenes, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEspacio);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EspacioResponse> actualizarEspacio(
            @PathVariable Long id,
            @RequestPart("espacio") String espacioJson,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes
    ) {
        try {
            Long usuarioId = getAuthenticatedUserId();
            
            // Debugging: Imprimir JSON recibido
            System.out.println("JSON RECIBIDO (UPDATE): " + espacioJson);

            EspacioActualizarRequest request = objectMapper.readValue(espacioJson, EspacioActualizarRequest.class);
            
            // Debugging: Imprimir objeto DTO
            System.out.println("DTO CONVERTIDO (UPDATE): " + request);

            EspacioResponse espacioActualizado = espacioService.actualizarEspacio(id, usuarioId, request, imagenes);
            return ResponseEntity.ok(espacioActualizado);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/pausar")
    public ResponseEntity<EspacioResponse> pausarEspacio(@PathVariable Long id) throws AccessDeniedException {
        Long usuarioId = getAuthenticatedUserId();
        EspacioResponse response = espacioService.pausarEspacio(id, usuarioId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/publicar")
    public ResponseEntity<EspacioResponse> publicarEspacio(@PathVariable Long id) throws AccessDeniedException {
        Long usuarioId = getAuthenticatedUserId();
        EspacioResponse response = espacioService.publicarEspacio(id, usuarioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<EspacioResponse>> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String servicios,
            @RequestParam(required = false) Integer capacidadMinima,
            Pageable pageable) {
        Long usuarioNavegandoId = null;
        try {
            usuarioNavegandoId = getAuthenticatedUserId();
        } catch (AccessDeniedException e) {
            // Usuario no autenticado, es válido para esta ruta
        }
        return ResponseEntity.ok(espacioService.listarEspacios(usuarioId, busqueda, tipo, servicios, capacidadMinima, usuarioNavegandoId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspacioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(espacioService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEspacio(@PathVariable Long id) throws AccessDeniedException {
        Long usuarioId = getAuthenticatedUserId();
        espacioService.eliminarEspacio(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    private Long getAuthenticatedUserId() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("Acceso denegado. Se requiere autenticación.");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
