package de.htw_berlin.kindercare.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    private static final String JSON_CONTENT_TYPE = "application/json";

    @Autowired MockMvc mockMvc;

    @Test
    void registersAndLogsInAUser() throws Exception {
        String account = "{\"email\":\"parent@example.test\",\"password\":\"SecurePass123\",\"role\":\"PARENT\"}";

        mockMvc.perform(post("/api/auth/register").contentType(JSON_CONTENT_TYPE).content(account))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("parent@example.test"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/api/auth/login").contentType(JSON_CONTENT_TYPE).content(account))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PARENT"));
    }

    @Test
    void rejectsDuplicateRegistration() throws Exception {
        String account = "{\"email\":\"duplicate@example.test\",\"password\":\"SecurePass123\",\"role\":\"PARENT\"}";

        mockMvc.perform(post("/api/auth/register").contentType(JSON_CONTENT_TYPE).content(account))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register").contentType(JSON_CONTENT_TYPE).content(account))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(JSON_CONTENT_TYPE)
                .content("{\"email\":\"missing@example.test\",\"password\":\"WrongPass123\"}"))
                .andExpect(status().isUnauthorized());
    }
}
