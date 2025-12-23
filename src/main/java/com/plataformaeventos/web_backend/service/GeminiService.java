package com.plataformaeventos.web_backend.service;

import com.plataformaeventos.web_backend.dto.ai.AiDescriptionResponse;
import com.plataformaeventos.web_backend.dto.ai.gemini.Candidate;
import com.plataformaeventos.web_backend.dto.ai.gemini.Content;
import com.plataformaeventos.web_backend.dto.ai.gemini.GeminiResponse;
import com.plataformaeventos.web_backend.dto.ai.gemini.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public AiDescriptionResponse generarDescripcion(String borrador) {
        // 1. Construir el Prompt con las nuevas reglas
        String prompt = String.format(
            "Actúa como el dueño de un espacio para eventos. Tu tarea es mejorar la redacción de este borrador: '%s'. " +
            "REGLAS ESTRICTAS: " +
            "1. NO inventes características, servicios ni instalaciones que no se mencionen explícitamente en el borrador. " +
            "2. La respuesta final NO debe superar los 1500 caracteres. " +
            "3. NO uses saltos de línea dobles entre párrafos, solo un salto de línea simple. " +
            "4. Responde ÚNICAMENTE con la descripción generada, sin añadir frases introductorias como 'Aquí tienes tu descripción' o similares. " +
            "ESTILO: " +
            "Tono: Profesional, cálido y confiable. " +
            "Idioma: Español Argentino. " +
            "NO uses emojis. " +
            "Resalta en **negrita** (usando asteriscos dobles) las palabras clave o características más importantes del espacio. " +
            "Corrige ortografía y gramática. No agregues comillas al inicio ni al final.",
            borrador
        );

        // 2. Construir el cuerpo JSON
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        // 3. Preparar Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String urlLimpia = apiUrl.trim();
        String keyLimpia = apiKey.trim();
        String urlFinal = urlLimpia + "?key=" + keyLimpia;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(urlFinal, entity, GeminiResponse.class);
            
            String textoGenerado = Optional.ofNullable(response.getBody())
                .map(GeminiResponse::getCandidates)
                .filter(candidates -> !candidates.isEmpty())
                .map(candidates -> candidates.get(0))
                .map(Candidate::getContent)
                .map(Content::getParts)
                .filter(parts -> !parts.isEmpty())
                .map(parts -> parts.get(0))
                .map(Part::getText)
                .orElseThrow(() -> new RuntimeException("La IA respondió OK, pero sin texto."));

            return new AiDescriptionResponse(textoGenerado);

        } catch (HttpClientErrorException e) {
            String errorDeGoogle = e.getResponseBodyAsString();
            System.err.println("ERROR GEMINI: " + errorDeGoogle);
            throw new RuntimeException("Google rechazó la petición: " + errorDeGoogle);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error interno: " + e.getMessage());
        }
    }
}
