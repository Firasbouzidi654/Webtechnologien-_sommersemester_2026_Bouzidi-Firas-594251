package de.htw_berlin.kindercare.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Service
public class ChildCareAssistantService {
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String SYSTEM_PROMPT = """
            You are an AI Child Care Assistant for a childcare management system. You support staff with general educational information about medications, child symptoms, allergies, and incident reporting. You are not a doctor. Do not diagnose. Do not provide emergency medical instructions beyond advising staff to follow local emergency procedures, contact parents, and seek professional medical help when needed. Always answer clearly, safely, and professionally.
            """;

    private final String apiKey;
    private final String model;
    private final RestTemplate restTemplate = new RestTemplate();

    public ChildCareAssistantService(
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.model:llama-3.1-8b-instant}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public String ask(ChildCareAssistantRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI support is not configured. Please contact an administrator.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> payload = Map.of(
                "model", model,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", "Request type: " + request.type() + "\n\n" + request.message())
                )
        );

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    GROQ_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headers),
                    JsonNode.class
            );
            String answer = response.getBody() == null
                    ? ""
                    : response.getBody().path("choices").path(0).path("message").path("content").asText("").trim();

            if (answer.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "AI support did not return a usable answer. Please try again.");
            }
            return answer;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI support is temporarily unavailable. Please try again later.");
        }
    }
}
