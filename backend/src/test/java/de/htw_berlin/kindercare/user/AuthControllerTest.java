package de.htw_berlin.kindercare.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void registersAndLogsInAUser() throws Exception {
        String account = "{\"email\":\"parent@example.test\",\"password\":\"SecurePass123\",\"role\":\"PARENT\"}";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(account))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("parent@example.test"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(account))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PARENT"));
    }
}
