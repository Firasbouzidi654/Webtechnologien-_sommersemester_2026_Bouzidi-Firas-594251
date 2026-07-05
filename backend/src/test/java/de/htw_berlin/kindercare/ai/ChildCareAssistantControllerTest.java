package de.htw_berlin.kindercare.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "groq.api.key=")
@AutoConfigureMockMvc
class ChildCareAssistantControllerTest {
    private static final String JSON_CONTENT_TYPE = "application/json";

    @Autowired MockMvc mockMvc;

    @Test
    void aiAssistantRequiresStaffRole() throws Exception {
        mockMvc.perform(post("/api/ai/childcare-assistant")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"type\":\"MEDICATION\",\"message\":\"Can this be given after lunch?\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void aiAssistantRejectsInvalidRequestType() throws Exception {
        mockMvc.perform(post("/api/ai/childcare-assistant").header("X-User-Role", "STAFF")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"type\":\"OTHER\",\"message\":\"Can this be given after lunch?\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aiAssistantReturnsServiceUnavailableWhenApiKeyIsMissing() throws Exception {
        mockMvc.perform(post("/api/ai/childcare-assistant").header("X-User-Role", "STAFF")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"type\":\"MEDICATION\",\"message\":\"Can this be given after lunch?\"}"))
                .andExpect(status().isServiceUnavailable());
    }
}
