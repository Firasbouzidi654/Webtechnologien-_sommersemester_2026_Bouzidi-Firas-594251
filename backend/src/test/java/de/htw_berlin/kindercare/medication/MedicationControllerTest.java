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
    void medicationLinksToChildByIdNotJustByName() throws Exception {
        String childResponse = mockMvc.perform(post("/api/children").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Lina\",\"allergies\":\"\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long childId = JsonPath.<Number>read(childResponse, "$.id").longValue();

        // The request sends a stale/wrong childName on purpose; the backend must
        // ignore it and use the real name from the child record via childId.
        String medicationResponse = mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + childId + ",\"childName\":\"Wrong Name\",\"dosage\":\"5 drops\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.childId").value(childId))
                .andExpect(jsonPath("$.childName").value("Lina"))
                .andReturn().getResponse().getContentAsString();

        long medicationId = JsonPath.<Number>read(medicationResponse, "$.id").longValue();

        mockMvc.perform(delete("/api/children/{id}", childId).header("X-User-Role", "PARENT"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/medications").header("X-User-Role", "PARENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + medicationId + ")]").isEmpty());
    }

    @Test
    void deletingChildDoesNotRemoveAnotherChildsMedicationWithTheSameName() throws Exception {
        long firstChildId = createChildNamed("Sam");
        long secondChildId = createChildNamed("Sam");

        createMedicationForChild(firstChildId, "Sam");
        long secondMedicationId = createMedicationForChild(secondChildId, "Sam");

        mockMvc.perform(delete("/api/children/{id}", firstChildId).header("X-User-Role", "PARENT"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/medications").header("X-User-Role", "PARENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + secondMedicationId + ")]").isNotEmpty());
    }

    private long createChildNamed(String name) throws Exception {
        String response = mockMvc.perform(post("/api/children").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"allergies\":\"\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(response, "$.id").longValue();
    }

    private long createMedicationForChild(long childId, String childName) throws Exception {
        String response = mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + childId + ",\"childName\":\"" + childName + "\",\"dosage\":\"5 drops\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(response, "$.id").longValue();
    }

    @Test
    void parentCanCreateButNotUpdateOrDeleteMedication() throws Exception {
        String response = mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Salbutamol\",\"childName\":\"Emma\",\"dosage\":\"1 puff\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number idValue = JsonPath.read(response, "$.id");
        long id = idValue.longValue();

        mockMvc.perform(put("/api/medications/{id}", id).header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"TAKEN\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/medications/{id}", id).header("X-User-Role", "PARENT"))
                .andExpect(status().isForbidden());
    }
}
