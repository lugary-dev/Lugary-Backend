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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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
    public EspacioResponse crearEspacio(EspacioCrearRequest request, List<MultipartFile> imagenes) throws IOException {
        if ("PUBLICADO".equalsIgnoreCase(request.getEstado())) {
            validarPublicacion(request);
        }

        Usuario propietario = usuarioRepository.findById(request.getPropietarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El propietario especificado no existe."));

        Espacio espacio = new Espacio();
        mapearRequestAEntidad(request, espacio);
        espacio.setPropietario(propietario);
        espacio.setFechaCreacion(LocalDateTime.now());
        
        if (imagenes != null && !imagenes.isEmpty()) {
            for (int i = 0; i < imagenes.size(); i++) {
                MultipartFile imgFile = imagenes.get(i);
                String url = cloudinaryService.subirImagen(imgFile);
                ImagenEspacio imagenEspacio = new ImagenEspacio();
                imagenEspacio.setUrl(url);
                imagenEspacio.setEspacio(espacio);
                imagenEspacio.setOrden(i);
                espacio.getImagenes().add(imagenEspacio);
            }
        }

        Espacio guardado = espacioRepository.save(espacio);
        return mapearAResponse(guardado);
    }

    @Transactional
    public EspacioResponse actualizarEspacio(Long espacioId, Long usuarioId, EspacioActualizarRequest request, List<MultipartFile> nuevasImagenes) throws IOException {
        if ("PUBLICADO".equalsIgnoreCase(request.getEstado())) {
            validarPublicacion(request);
        }

        Espacio espacio = findEspacioByIdAndPropietario(espacioId, usuarioId);
        mapearRequestAEntidad(request, espacio);

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
        return mapearAResponse(actualizado);
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
            espacio.setEstado(EstadoEspacio.valueOf(r.getEstado().toUpperCase()));
            
            // Nuevos campos
            espacio.setAvisoMinimoHoras(r.getAvisoMinimoHoras());
            espacio.setAnticipacionMaximaMeses(r.getAnticipacionMaximaMeses());
            espacio.setEstadiaMinima(r.getEstadiaMinima());
            espacio.setHoraCheckIn(r.getHoraCheckIn());
            espacio.setHoraCheckOut(r.getHoraCheckOut());
            espacio.setDiasBloqueados(convertirListaAString(r.getDiasBloqueados()));
            espacio.setPermiteReservasInvitado(r.getPermiteReservasInvitado());

        } else if (request instanceof EspacioActualizarRequest r) {
            espacio.setNombre(r.getNombre());
            espacio.setDescripcion(r.getDescripcion());
            espacio.setTipo(r.getTipo());
            espacio.setDireccion(r.getDireccion());
            espacio.setCapacidadMaxima(r.getCapacidadMaxima());
            espacio.setPrecio(r.getPrecio());
            espacio.setUnidadPrecio(r.getUnidadPrecio());
            espacio.setServicios(convertirListaAString(r.getServicios()));
            espacio.setReglas(convertirListaAString(r.getReglas()));
            espacio.setEstado(EstadoEspacio.valueOf(r.getEstado().toUpperCase()));

            // Nuevos campos
            espacio.setAvisoMinimoHoras(r.getAvisoMinimoHoras());
            espacio.setAnticipacionMaximaMeses(r.getAnticipacionMaximaMeses());
            espacio.setEstadiaMinima(r.getEstadiaMinima());
            espacio.setHoraCheckIn(r.getHoraCheckIn());
            espacio.setHoraCheckOut(r.getHoraCheckOut());
            espacio.setDiasBloqueados(convertirListaAString(r.getDiasBloqueados()));
            espacio.setPermiteReservasInvitado(r.getPermiteReservasInvitado());
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

        return espacioRepository.findAll(spec, pageable).map(this::mapearAResponse);
    }

    @Transactional
    public EspacioResponse pausarEspacio(Long espacioId, Long usuarioId) {
        Espacio espacio = findEspacioByIdAndPropietario(espacioId, usuarioId);
        espacio.setEstado(EstadoEspacio.PAUSADO);
        Espacio guardado = espacioRepository.save(espacio);
        return mapearAResponse(guardado);
    }

    @Transactional
    public EspacioResponse publicarEspacio(Long espacioId, Long usuarioId) {
        Espacio espacio = findEspacioByIdAndPropietario(espacioId, usuarioId);
        espacio.setEstado(EstadoEspacio.PUBLICADO);
        Espacio guardado = espacioRepository.save(espacio);
        return mapearAResponse(guardado);
    }

    public EspacioResponse obtenerPorId(Long id) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un espacio con el ID especificado."));
        return mapearAResponse(espacio);
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

    private EspacioResponse mapearAResponse(Espacio espacio) {
        List<String> urls = espacio.getImagenes().stream()
                                    .map(ImagenEspacio::getUrl)
                                    .collect(Collectors.toList());

        EspacioConfig config = EspacioConfig.builder()
                .unidadPrecio(espacio.getUnidadPrecio())
                .avisoMinimoHoras(espacio.getAvisoMinimoHoras())
                .anticipacionMaximaMeses(espacio.getAnticipacionMaximaMeses())
                .estadiaMinima(espacio.getEstadiaMinima())
                .horaCheckIn(espacio.getHoraCheckIn())
                .horaCheckOut(espacio.getHoraCheckOut())
                .diasBloqueados(convertirStringALista(espacio.getDiasBloqueados()))
                .permiteReservasInvitado(espacio.getPermiteReservasInvitado())
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
        // Eliminar duplicados si los hubiera
        fechasOcupadas = fechasOcupadas.stream().distinct().collect(Collectors.toList());

        return EspacioResponse.builder()
                .id(espacio.getId())
                .nombre(espacio.getNombre())
                .descripcion(espacio.getDescripcion())
                .tipo(espacio.getTipo())
                .direccion(espacio.getDireccion())
                .capacidadMaxima(espacio.getCapacidadMaxima())
                .precio(espacio.getPrecio())
                // .unidadPrecio(espacio.getUnidadPrecio()) // Ahora está en config
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

    private String convertirListaAString(List<String> lista) {
        return (lista == null || lista.isEmpty()) ? null : String.join(",", lista);
    }

    private List<String> convertirStringALista(String texto) {
        return (!StringUtils.hasText(texto)) ? Collections.emptyList() : Arrays.asList(texto.split(","));
    }
}
