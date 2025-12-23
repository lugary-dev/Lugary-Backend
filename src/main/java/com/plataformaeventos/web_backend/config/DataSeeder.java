package com.plataformaeventos.web_backend.config;

import com.plataformaeventos.web_backend.model.Espacio;
import com.plataformaeventos.web_backend.model.EstadoEspacio;
import com.plataformaeventos.web_backend.model.ImagenEspacio;
import com.plataformaeventos.web_backend.model.Usuario;
import com.plataformaeventos.web_backend.repository.EspacioRepository;
import com.plataformaeventos.web_backend.repository.UsuarioRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final Faker faker;

    private static final Map<String, List<String>> IMAGENES_POR_TIPO = new HashMap<>();

    static {
        IMAGENES_POR_TIPO.put("Quincho", Arrays.asList(
            "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&q=80",
            "https://images.unsplash.com/photo-1595596005709-66e612ea2c36?w=800&q=80",
            "https://images.unsplash.com/photo-1534353436294-0dbd4bdac845?w=800&q=80"
        ));
        IMAGENES_POR_TIPO.put("Salón de Fiestas", Arrays.asList(
            "https://images.unsplash.com/photo-1519167758481-83f550bb49b3?w=800&q=80",
            "https://images.unsplash.com/photo-1464366400600-7168b8af0bc3?w=800&q=80",
            "https://images.unsplash.com/photo-1514525253440-b393452e8d26?w=800&q=80"
        ));
        IMAGENES_POR_TIPO.put("Casaquinta / Parque", Arrays.asList(
            "https://images.unsplash.com/photo-1566737236500-c8ac43014a67?w=800&q=80",
            "https://images.unsplash.com/photo-1621293954908-35688043628e?w=800&q=80",
            "https://images.unsplash.com/photo-1572331165267-854960822952?w=800&q=80"
        ));
        IMAGENES_POR_TIPO.put("General", Arrays.asList(
            "https://images.unsplash.com/photo-1449844908441-8829872d2607?w=800&q=80",
            "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800&q=80",
            "https://images.unsplash.com/photo-1596276122653-651a3898309f?w=800&q=80"
        ));
    }

    public DataSeeder(EspacioRepository espacioRepository, UsuarioRepository usuarioRepository) {
        this.espacioRepository = espacioRepository;
        this.usuarioRepository = usuarioRepository;
        this.faker = new Faker(new Locale("es", "AR"));
    }

    @Override
    public void run(String... args) throws Exception {
        // Cambiamos la condición para que se ejecute si hay 0 espacios
        if (espacioRepository.count() > 0) { // Si hay más de 0 espacios, no sembramos
            System.out.println("🌱 La base de datos ya tiene espacios. Saltando seeding.");
            return;
        }

        System.out.println("🌱 Sembrando 80 espacios con estética Argentina Pinterest...");

        Usuario propietario = usuarioRepository.findById(1L).orElseGet(() -> {
            List<Usuario> usuarios = usuarioRepository.findAll();
            return usuarios.isEmpty() ? null : usuarios.get(0);
        });

        if (propietario == null) {
            System.out.println("⚠️ No hay usuarios. Crea uno primero.");
            return;
        }

        String[] tiposDisponibles = {"Quincho", "Salón de Fiestas", "Casaquinta / Parque", "Loft", "Cabaña"};
        Random random = new Random();

        for (int i = 0; i < 80; i++) {
            Espacio e = new Espacio();
            String tipo = tiposDisponibles[random.nextInt(tiposDisponibles.length)];
            e.setTipo(tipo);

            List<String> poolDeImagenes = IMAGENES_POR_TIPO.getOrDefault(tipo, IMAGENES_POR_TIPO.get("General"));
            String imagenElegida = poolDeImagenes.get(random.nextInt(poolDeImagenes.size()));
            
            ImagenEspacio imagen = new ImagenEspacio();
            imagen.setUrl(imagenElegida);
            imagen.setOrden(0);
            imagen.setEspacio(e);
            e.getImagenes().add(imagen);

            String nombreFantasia = tipo + " " + faker.funnyName().name();
            e.setNombre(nombreFantasia);
            e.setDescripcion("Espacio ideal para tu evento. " + faker.lorem().paragraph(2));
            
            String[] provincias = {"Mendoza", "Buenos Aires", "Córdoba", "Santa Fe"};
            String provincia = provincias[random.nextInt(provincias.length)];
            e.setDireccion(faker.address().streetAddress() + " " + faker.address().buildingNumber() + ", " + faker.address().cityName() + ", " + provincia);

            e.setCapacidadMaxima(15 + random.nextInt(200));

            String[] unidades = {"HORA", "DIA", "PERSONA"};
            String unidad = unidades[random.nextInt(unidades.length)];
            e.setUnidadPrecio(unidad);

            double precioBase = switch (unidad) {
                case "HORA" -> 5000 + random.nextInt(15000);
                case "PERSONA" -> 3500 + random.nextInt(6000);
                default -> 90000 + random.nextInt(150000);
            };
            
            e.setPrecio(BigDecimal.valueOf(precioBase));
            e.setEstado(EstadoEspacio.PUBLICADO);
            e.setPropietario(propietario);
            e.setFechaCreacion(LocalDateTime.now().minusDays(random.nextInt(100)));
            e.setServicios("Wifi,Parrilla,Estacionamiento,Aire Acondicionado");
            e.setReglas("No fumar en interiores,Cuidar las instalaciones");

            espacioRepository.save(e);
        }

        System.out.println("✅ ¡80 Espacios estilo Pinterest creados!");
    }
}
