package de.htw_berlin.kindercare.medication;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MedicationControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void staffCanCreateUpdateAndDeleteMedication() throws Exception {
        String response = mockMvc.perform(post("/api/medications").header("X-User-Role", "STAFF")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Salbutamol\",\"childName\":\"Emma\",\"dosage\":\"1 puff\",\"time\":\"08:30\",\"status\":\"PENDING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.time").value("08:30"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        Number idValue = JsonPath.read(response, "$.id");
        long id = idValue.longValue();

        mockMvc.perform(get("/api/medications").header("X-User-Role", "PARENT"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/medications/{id}", id).header("X-User-Role", "STAFF")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"time\":\"09:00\",\"status\":\"TAKEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").value("09:00"))
                .andExpect(jsonPath("$.status").value("TAKEN"));

        mockMvc.perform(delete("/api/medications/{id}", id).header("X-User-Role", "STAFF"))
                .andExpect(status().isNoContent());
    }

    @Test
    void parentCannotManageMedication() throws Exception {
        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Salbutamol\",\"childName\":\"Emma\",\"dosage\":\"1 puff\"}"))
                .andExpect(status().isForbidden());
    }
}
