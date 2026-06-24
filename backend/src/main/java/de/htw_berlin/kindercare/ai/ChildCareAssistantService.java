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
            You are an AI Child Care Assistant for a childcare management system. You provide concise, general educational support about medications, child symptoms, allergies, and incident reporting for childcare staff.

            You are not a doctor. Do not diagnose, prescribe treatment, provide dosage guidance, or give detailed medical treatment instructions. Do not mention specific diseases unless the staff explicitly asks about one. For urgent concerns in Berlin, Germany, advise staff to follow local emergency procedures, contact parents, and seek professional medical help when needed. Use 112 for medical or fire emergencies and 110 for police emergencies. Never mention or recommend calling 911.

            Always use exactly these four short sections:
            1. Summary
            2. Safe next steps for staff
            3. When to contact parents/medical help
            4. Documentation note

            Keep the response concise, practical, clear, and professional. Always end with: "AI support is for educational purposes only and does not replace professional medical advice."
            """;
    private static final String PARENT_MESSAGE_SYSTEM_PROMPT = """
            You are an assistant helping childcare staff write professional parent communications.

            Write clear, friendly, professional, and reassuring messages in simple language. Focus only on facts provided by staff. Do not diagnose medical conditions, provide medical advice, create panic, or invent missing details. Keep the message between 50 and 120 words.

            Always use exactly this format:
            Dear Parent,

            [message]

            Kind regards,
            KinderCare Staff
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
        return requestCompletion(SYSTEM_PROMPT, "Request type: " + request.type() + "\n\n" + request.message());
    }

    public String generateParentMessage(String message) {
        return requestCompletion(PARENT_MESSAGE_SYSTEM_PROMPT, message);
    }

    private String requestCompletion(String systemPrompt, String userMessage) {
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
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
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

    public boolean isGroqConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getModel() {
        return model;
    }
}
