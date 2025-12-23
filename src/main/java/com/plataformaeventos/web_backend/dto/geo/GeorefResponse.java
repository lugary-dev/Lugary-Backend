package com.plataformaeventos.web_backend.dto.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeorefResponse {
    private List<Localidad> localidades;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Localidad {
        private String id;
        private String nombre;
        private Provincia provincia;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Provincia {
        private String id;
        private String nombre;
    }
}
