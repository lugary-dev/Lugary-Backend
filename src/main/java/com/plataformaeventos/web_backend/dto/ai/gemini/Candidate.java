package com.plataformaeventos.web_backend.dto.ai.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Candidate {
    private Content content;
}
