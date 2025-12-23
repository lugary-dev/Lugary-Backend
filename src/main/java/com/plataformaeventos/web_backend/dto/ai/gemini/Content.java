package com.plataformaeventos.web_backend.dto.ai.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Content {
    private List<Part> parts;
}
