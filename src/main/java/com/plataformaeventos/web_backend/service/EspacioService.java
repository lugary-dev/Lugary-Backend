package com.plataformaeventos.web_backend.service;

import com.plataformaeventos.web_backend.dto.EspacioActualizarRequest;
import com.plataformaeventos.web_backend.dto.EspacioConfig;
import com.plataformaeventos.web_backend.dto.EspacioCrearRequest;
import com.plataformaeventos.web_backend.dto.EspacioResponse;
import com.plataformaeventos.web_backend.exception.DatosInvalidosException;
import com.plataformaeventos.web_backend.exception.RecursoNoEncontradoException;
import com.plataformaeventos.web_backend.model.*;
import com.plataformaeventos.web_backend.repository.EspacioRepository;
import com.plataformaeventos.web_backend.repository.ReservaRepository;
import com.plataformaeventos.web_backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EspacioService {

    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public EspacioResponse crearEspacio(EspacioCrearRequest request, List<MultipartFile> imagenes, Long propietarioId) throws IOException {
        // Validar lógica de fechas
        validarHorarios(request.getHoraCheckIn(), request.getHoraCheckOut());

        Usuario propietario = usuarioRepository.findById(propietarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El propietario especificado no existe."));

        if ("PUBLICADO".equalsIgnoreCase(request.getEstado())) {
            validarPublicacion(request);
        }

        Espacio espacio = new Espacio();
        mapearRequestAEntidad(request, espacio);
        espacio.setPropietario(propietario);
        espacio.setFechaCreacion(LocalDateTime.now());
        
        // Gestión de Imágenes con validación de Magic Bytes
        if (imagenes != null && !imagenes.isEmpty()) {
            validarImagenes(imagenes);
            
            // Reordenar imágenes según imageOrder si existe
            List<MultipartFile> imagenesOrdenadas = ordenarImagenes(imagenes, request.getImageOrder());

            for (int i = 0; i < imagenesOrdenadas.size(); i++) {
                MultipartFile imgFile = imagenesOrdenadas.get(i);
                String url = cloudinaryService.subirImagen(imgFile);
                ImagenEspacio imagenEspacio = new ImagenEspacio();
                imagenEspacio.setUrl(url);
                imagenEspacio.setEspacio(espacio);
                imagenEspacio.setOrden(i);
                espacio.getImagenes().add(imagenEspacio);
            }
        }

        Espacio guardado = espacioRepository.save(espacio);
        return mapearAResponse(guardado, propietarioId);
    }

    @Transactional
    public EspacioResponse actualizarEspacio(Long espacioId, Long usuarioId, EspacioActualizarRequest request, List<MultipartFile> nuevasImagenes) throws IOException {
        // Validar lógica de fechas si se actualizan
        if (request.getHoraCheckIn() != null && request.getHoraCheckOut() != null) {
            validarHorarios(request.getHoraCheckIn(), request.getHoraCheckOut());
        }

        if ("PUBLICADO".equalsIgnoreCase(request.getEstado())) {
            validarPublicacion(request);
        }

        Espacio espacio = findEspacioByIdAndPropietario(espacioId, usuarioId);
        mapearRequestAEntidad(request, espacio);

        // Gestión de Imágenes
        if (nuevasImagenes != null && !nuevasImagenes.isEmpty()) {
            validarImagenes(nuevasImagenes);
        }

        List<String> imageOrder = request.getImageOrder();
        if (imageOrder != null) {
            List<ImagenEspacio> galeriaFinal = new ArrayList<>();
            Map<String, MultipartFile> mapaNuevasImagenes = (nuevasImagenes != null)
                    ? nuevasImagenes.stream().collect(Collectors.toMap(MultipartFile::getOriginalFilename, Function.identity()))
                    : Collections.emptyMap();

            int orden = 0;
            for (String item : imageOrder) {
                ImagenEspacio imagenParaGuardar = new ImagenEspacio();
                if (item.startsWith("http")) {
                    imagenParaGuardar = espacio.getImagenes().stream()
                            .filter(img -> img.getUrl().equals(item))
                            .findFirst()
                            .orElse(new ImagenEspacio());
                    imagenParaGuardar.setUrl(item);
                } else {
                    MultipartFile archivoNuevo = mapaNuevasImagenes.get(item);
                    if (archivoNuevo != null) {
                        imagenParaGuardar.setUrl(cloudinaryService.subirImagen(archivoNuevo));
                    }
                }
                
                imagenParaGuardar.setOrden(orden++);
                imagenParaGuardar.setEspacio(espacio);
                galeriaFinal.add(imagenParaGuardar);
            }
            espacio.getImagenes().clear();
            espacio.getImagenes().addAll(galeriaFinal);
        }

        Espacio actualizado = espacioRepository.save(espacio);
        return mapearAResponse(actualizado, usuarioId);
    }

    private void validarHorarios(LocalTime checkIn, LocalTime checkOut) {
        if (checkIn == null || checkOut == null) return;
        // Validar que checkIn y checkOut sean lógicos si es necesario
    }

    private void validarImagenes(List<MultipartFile> imagenes) {
        for (MultipartFile file : imagenes) {
            try {
                // Validación básica de Magic Bytes (primeros bytes del archivo)
                byte[] magicBytes = file.getInputStream().readNBytes(4);
                if (!esImagenValida(magicBytes)) {
                    throw new DatosInvalidosException("El archivo " + file.getOriginalFilename() + " no es una imagen válida o está corrupto.");
                }
            } catch (IOException e) {
                throw new DatosInvalidosException("Error al leer el archivo " + file.getOriginalFilename());
            }
        }
    }

    private boolean esImagenValida(byte[] magicBytes) {
        if (magicBytes.length < 4) return false;
        
        // Hex signatures
        // JPEG: FF D8 FF
        // PNG: 89 50 4E 47
        // WEBP: RIFF ... WEBP (más complejo, simplificado aquí)
        
        if (magicBytes[0] == (byte) 0xFF && magicBytes[1] == (byte) 0xD8 && magicBytes[2] == (byte) 0xFF) return true; // JPEG
        if (magicBytes[0] == (byte) 0x89 && magicBytes[1] == (byte) 0x50 && magicBytes[2] == (byte) 0x4E && magicBytes[3] == (byte) 0x47) return true; // PNG
        
        // Simple check for RIFF (WebP starts with RIFF)
        if (magicBytes[0] == (byte) 0x52 && magicBytes[1] == (byte) 0x49 && magicBytes[2] == (byte) 0x46 && magicBytes[3] == (byte) 0x46) return true;

        return false;
    }

    private List<MultipartFile> ordenarImagenes(List<MultipartFile> imagenes, List<String> imageOrder) {
        if (imageOrder == null || imageOrder.isEmpty()) {
            return imagenes;
        }
        
        Map<String, MultipartFile> map = imagenes.stream()
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, Function.identity()));
        
        List<MultipartFile> ordenadas = new ArrayList<>();
        for (String name : imageOrder) {
            if (map.containsKey(name)) {
                ordenadas.add(map.get(name));
            }
        }
        // Agregar las que no estaban en el orden al final (por seguridad)
        for (MultipartFile file : imagenes) {
            if (!ordenadas.contains(file)) {
                ordenadas.add(file);
            }
        }
        return ordenadas;
    }

    private void validarPublicacion(Object request) {
        if (request instanceof EspacioCrearRequest r) {
            if (r.getPrecio() == null || r.getPrecio().doubleValue() <= 0) throw new DatosInvalidosException("Para publicar, el precio es obligatorio.");
            if (r.getCapacidadMaxima() == null || r.getCapacidadMaxima() <= 0) throw new DatosInvalidosException("Para publicar, la capacidad es obligatoria.");
            if (r.getImageOrder() == null || r.getImageOrder().isEmpty()) throw new DatosInvalidosException("Debes subir al menos una foto para publicar.");
        } else if (request instanceof EspacioActualizarRequest r) {
            if (r.getPrecio() == null || r.getPrecio().doubleValue() <= 0) throw new DatosInvalidosException("Para publicar, el precio es obligatorio.");
            if (r.getCapacidadMaxima() == null || r.getCapacidadMaxima() <= 0) throw new DatosInvalidosException("Para publicar, la capacidad es obligatoria.");
            if (r.getImageOrder() == null || r.getImageOrder().isEmpty()) throw new DatosInvalidosException("Debes subir al menos una foto para publicar.");
        }
    }

    private void mapearRequestAEntidad(Object request, Espacio espacio) {
        if (request instanceof EspacioCrearRequest r) {
            espacio.setNombre(r.getNombre());
            espacio.setDescripcion(r.getDescripcion());
            espacio.setTipo(r.getTipo());
            espacio.setDireccion(r.getDireccion());
            espacio.setCapacidadMaxima(r.getCapacidadMaxima());
            espacio.setPrecio(r.getPrecio());
            espacio.setUnidadPrecio(r.getUnidadPrecio());
            espacio.setServicios(convertirListaAString(r.getServicios())); 
            espacio.setReglas(convertirListaAString(r.getReglas()));
            if (r.getEstado() != null) espacio.setEstado(EstadoEspacio.valueOf(r.getEstado().toUpperCase()));
            
            // Nuevos campos
            espacio.setLatitud(r.getLatitud());
            espacio.setLongitud(r.getLongitud());
            espacio.setGooglePlaceId(r.getGooglePlaceId());
            espacio.setReferencia(r.getReferencia());
            
            espacio.setPrecioFinDeSemana(r.getPrecioFinDeSemana());
            espacio.setCargoLimpieza(r.getCargoLimpieza());
            espacio.setMontoDeposito(r.getMontoDeposito());
            espacio.setCobroDeposito(r.getCobroDeposito());
            
            espacio.setHoraCheckIn(r.getHoraCheckIn());
            espacio.setHoraCheckOut(r.getHoraCheckOut());
            espacio.setTiempoPreparacion(r.getTiempoPreparacion());
            espacio.setAvisoMinimo(r.getAvisoMinimo());
            espacio.setAnticipacionMaxima(r.getAnticipacionMaxima());
            espacio.setEstadiaMinima(r.getEstadiaMinima());
            espacio.setDiasBloqueados(r.getDiasBloqueados());
            
            espacio.setTipoReserva(r.getTipoReserva());
            espacio.setPoliticaCancelacion(r.getPoliticaCancelacion());
            espacio.setMostrarDireccionExacta(r.getMostrarDireccionExacta());
            espacio.setAcceptUnverifiedUsers(r.getAcceptUnverifiedUsers());
            espacio.setPermiteEstadiaNocturna(r.getPermiteEstadiaNocturna());
            espacio.setPermiteReservasInvitado(r.getPermiteReservasInvitado());
            
            // NUEVO: Modo de Reserva
            espacio.setModoReserva(r.getModoReserva());

        } else if (request instanceof EspacioActualizarRequest r) {
            // Campos Básicos
            if (r.getNombre() != null) espacio.setNombre(r.getNombre());
            if (r.getDescripcion() != null) espacio.setDescripcion(r.getDescripcion());
            if (r.getTipo() != null) espacio.setTipo(r.getTipo());
            if (r.getDireccion() != null) espacio.setDireccion(r.getDireccion());
            if (r.getCapacidadMaxima() != null) espacio.setCapacidadMaxima(r.getCapacidadMaxima());
            if (r.getPrecio() != null) espacio.setPrecio(r.getPrecio());
            if (r.getUnidadPrecio() != null) espacio.setUnidadPrecio(r.getUnidadPrecio());
            if (r.getServicios() != null) espacio.setServicios(convertirListaAString(r.getServicios()));
            if (r.getReglas() != null) espacio.setReglas(convertirListaAString(r.getReglas()));
            if (r.getEstado() != null) espacio.setEstado(EstadoEspacio.valueOf(r.getEstado().toUpperCase()));

            // --- UBICACIÓN ---
            if (r.getLatitud() != null) espacio.setLatitud(r.getLatitud());
            if (r.getLongitud() != null) espacio.setLongitud(r.getLongitud());
            if (r.getGooglePlaceId() != null) espacio.setGooglePlaceId(r.getGooglePlaceId());
            if (r.getReferencia() != null) espacio.setReferencia(r.getReferencia());

            // --- PRECIOS ---
            if (r.getPrecioFinDeSemana() != null) espacio.setPrecioFinDeSemana(r.getPrecioFinDeSemana());
            if (r.getCargoLimpieza() != null) espacio.setCargoLimpieza(r.getCargoLimpieza());
            if (r.getMontoDeposito() != null) espacio.setMontoDeposito(r.getMontoDeposito());
            if (r.getCobroDeposito() != null) espacio.setCobroDeposito(r.getCobroDeposito());

            // --- REGLAS ---
            if (r.getHoraCheckIn() != null) espacio.setHoraCheckIn(r.getHoraCheckIn());
            if (r.getHoraCheckOut() != null) espacio.setHoraCheckOut(r.getHoraCheckOut());
            if (r.getTiempoPreparacion() != null) espacio.setTiempoPreparacion(r.getTiempoPreparacion());
            if (r.getAvisoMinimo() != null) espacio.setAvisoMinimo(r.getAvisoMinimo());
            if (r.getAnticipacionMaxima() != null) espacio.setAnticipacionMaxima(r.getAnticipacionMaxima());
            if (r.getEstadiaMinima() != null) espacio.setEstadiaMinima(r.getEstadiaMinima());
            if (r.getDiasBloqueados() != null) espacio.setDiasBloqueados(r.getDiasBloqueados());

            // --- CONFIGURACIÓN ---
            if (r.getTipoReserva() != null) espacio.setTipoReserva(r.getTipoReserva());
            if (r.getPoliticaCancelacion() != null) espacio.setPoliticaCancelacion(r.getPoliticaCancelacion());
            if (r.getMostrarDireccionExacta() != null) espacio.setMostrarDireccionExacta(r.getMostrarDireccionExacta());
            if (r.getAcceptUnverifiedUsers() != null) espacio.setAcceptUnverifiedUsers(r.getAcceptUnverifiedUsers());
            if (r.getPermiteEstadiaNocturna() != null) espacio.setPermiteEstadiaNocturna(r.getPermiteEstadiaNocturna());
            if (r.getPermiteReservasInvitado() != null) espacio.setPermiteReservasInvitado(r.getPermiteReservasInvitado());
            
            // NUEVO: Modo de Reserva
            if (r.getModoReserva() != null) espacio.setModoReserva(r.getModoReserva());
        }
    }
    
    public Page<EspacioResponse> listarEspacios(Long usuarioId, String busqueda, String tipo, String servicios, Integer capacidadMinima, Long usuarioNavegandoId, Pageable pageable) {
        Specification<Espacio> spec = (root, query, cb) -> cb.conjunction();

        if (usuarioId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("propietario").get("id"), usuarioId));
        } else {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), EstadoEspacio.PUBLICADO));
            if (usuarioNavegandoId != null) {
                spec = spec.and((root, query, cb) -> cb.notEqual(root.get("propietario").get("id"), usuarioNavegandoId));
            }
        }
        
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                String pattern = "%" + busqueda.trim().toLowerCase() + "%";
                return cb.or(
                        cb.like(cb.lower(root.get("nombre")), pattern),
                        cb.like(cb.lower(root.get("descripcion")), pattern)
                );
            });
        }
        if (tipo != null && !tipo.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("tipo"), "%" + tipo + "%"));
        }
        if (servicios != null && !servicios.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("servicios"), "%" + servicios + "%"));
        }
        if (capacidadMinima != null && capacidadMinima > 0) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("capacidadMaxima"), capacidadMinima));
        }
        
        return espacioRepository.findAll(spec, pageable).map(espacio -> mapearAResponse(espacio, usuarioNavegandoId));
    }

    @Transactional
    public EspacioResponse pausarEspacio(Long espacioId, Long usuarioId) {
        Espacio espacio = findEspacioByIdAndPropietario(espacioId, usuarioId);
        espacio.setEstado(EstadoEspacio.PAUSADO);
        Espacio guardado = espacioRepository.save(espacio);
        return mapearAResponse(guardado, usuarioId);
    }

    @Transactional
    public EspacioResponse publicarEspacio(Long espacioId, Long usuarioId) {
        Espacio espacio = findEspacioByIdAndPropietario(espacioId, usuarioId);
        espacio.setEstado(EstadoEspacio.PUBLICADO);
        Espacio guardado = espacioRepository.save(espacio);
        return mapearAResponse(guardado, usuarioId);
    }

    public EspacioResponse obtenerPorId(Long id) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un espacio con el ID especificado."));
        
        Long usuarioId = null;
        try {
            usuarioId = getAuthenticatedUserId();
        } catch (Exception e) {
            // Usuario anónimo
        }
        
        return mapearAResponse(espacio, usuarioId);
    }

    @Transactional
    public void eliminarEspacio(Long id, Long propietarioId) {
        Espacio espacio = findEspacioByIdAndPropietario(id, propietarioId);
        espacioRepository.delete(espacio);
    }
    
    private Espacio findEspacioByIdAndPropietario(Long espacioId, Long usuarioId) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El espacio no existe."));
        if (!espacio.getPropietario().getId().equals(usuarioId)) {
            throw new DatosInvalidosException("No tienes permisos sobre este espacio.");
        }
        return espacio;
    }

    private EspacioResponse mapearAResponse(Espacio espacio, Long usuarioConsultanteId) {
        List<String> urls = espacio.getImagenes().stream()
                                    .map(ImagenEspacio::getUrl)
                                    .collect(Collectors.toList());

        // Lógica de Privacidad para Dirección
        String direccionMostrada = espacio.getDireccion();
        Double latitudMostrada = espacio.getLatitud();
        Double longitudMostrada = espacio.getLongitud();
        String referenciaMostrada = espacio.getReferencia();

        boolean esPropietario = usuarioConsultanteId != null && usuarioConsultanteId.equals(espacio.getPropietario().getId());
        boolean tieneReservaConfirmada = false;
        if (usuarioConsultanteId != null && !esPropietario) {
            // Verificar si tiene reserva confirmada (lógica simplificada)
            // tieneReservaConfirmada = reservaRepository.existsByEspacioAndUsuarioAndEstado(espacio, usuarioConsultanteId, EstadoReserva.CONFIRMADA);
        }

        if (espacio.getMostrarDireccionExacta() == VisibilidadDireccion.APROXIMADA && !esPropietario && !tieneReservaConfirmada) {
            direccionMostrada = null; 
            referenciaMostrada = null;
            
            // Fuzzing de coordenadas
            if (latitudMostrada != null && longitudMostrada != null) {
                double fuzzLat = (Math.random() - 0.5) * 0.004; 
                double fuzzLng = (Math.random() - 0.5) * 0.004;
                latitudMostrada += fuzzLat;
                longitudMostrada += fuzzLng;
            }
        }

        EspacioConfig config = EspacioConfig.builder()
                .unidadPrecio(espacio.getUnidadPrecio())
                .latitud(latitudMostrada)
                .longitud(longitudMostrada)
                .googlePlaceId(esPropietario || tieneReservaConfirmada ? espacio.getGooglePlaceId() : null)
                .referencia(referenciaMostrada)
                .precioFinDeSemana(espacio.getPrecioFinDeSemana())
                .cargoLimpieza(espacio.getCargoLimpieza())
                .montoDeposito(espacio.getMontoDeposito())
                .cobroDeposito(espacio.getCobroDeposito())
                .horaCheckIn(espacio.getHoraCheckIn())
                .horaCheckOut(espacio.getHoraCheckOut())
                .tiempoPreparacion(espacio.getTiempoPreparacion())
                .avisoMinimo(espacio.getAvisoMinimo())
                .anticipacionMaxima(espacio.getAnticipacionMaxima())
                .estadiaMinima(espacio.getEstadiaMinima())
                .diasBloqueados(espacio.getDiasBloqueados())
                .tipoReserva(espacio.getTipoReserva())
                .politicaCancelacion(espacio.getPoliticaCancelacion())
                .mostrarDireccionExacta(espacio.getMostrarDireccionExacta())
                .acceptUnverifiedUsers(espacio.getAcceptUnverifiedUsers())
                .permiteEstadiaNocturna(espacio.getPermiteEstadiaNocturna())
                .permiteReservasInvitado(espacio.getPermiteReservasInvitado())
                .modoReserva(espacio.getModoReserva()) // NUEVO
                .build();

        // Obtener fechas ocupadas
        List<String> fechasOcupadas = new ArrayList<>();
        List<Reserva> reservas = reservaRepository.findByEspacioAndEstadoNot(espacio, EstadoReserva.CANCELADA);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Reserva r : reservas) {
            LocalDateTime current = r.getFechaInicio();
            while (current.isBefore(r.getFechaFin()) || current.isEqual(r.getFechaFin())) {
                fechasOcupadas.add(current.format(formatter));
                current = current.plusDays(1);
            }
        }
        fechasOcupadas = fechasOcupadas.stream().distinct().collect(Collectors.toList());

        return EspacioResponse.builder()
                .id(espacio.getId())
                .nombre(espacio.getNombre())
                .descripcion(espacio.getDescripcion())
                .tipo(espacio.getTipo())
                .direccion(direccionMostrada)
                .capacidadMaxima(espacio.getCapacidadMaxima())
                .precio(espacio.getPrecio())
                .estado(espacio.getEstado().name())
                .propietarioId(espacio.getPropietario().getId())
                .fechaCreacion(espacio.getFechaCreacion())
                .imagenes(urls)
                .servicios(convertirStringALista(espacio.getServicios()))
                .reglas(convertirStringALista(espacio.getReglas()))
                .config(config)
                .fechasOcupadas(fechasOcupadas)
                .build();
    }

    private String convertirListaAString(Object listaObj) {
        if (listaObj == null) return null;
        if (listaObj instanceof String) return (String) listaObj;
        if (listaObj instanceof List) {
            List<?> lista = (List<?>) listaObj;
            if (lista.isEmpty()) return null;
            return lista.stream().map(Object::toString).collect(Collectors.joining(","));
        }
        return null;
    }

    private List<String> convertirStringALista(String texto) {
        return (!StringUtils.hasText(texto)) ? Collections.emptyList() : Arrays.asList(texto.split(","));
    }
    
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        try {
             Object principal = authentication.getPrincipal();
             // Asumiendo que CustomUserDetails tiene un método getId()
             if (principal instanceof com.plataformaeventos.web_backend.config.CustomUserDetails) {
                 return ((com.plataformaeventos.web_backend.config.CustomUserDetails) principal).getId();
             }
        } catch (Exception e) {
            // Fallback
        }
        throw new AccessDeniedException("No se pudo obtener el ID del usuario autenticado");
    }
}
