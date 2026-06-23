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
        long childId = createChildNamed("Emma");
        String response = mockMvc.perform(post("/api/medications").header("X-User-Role", "STAFF")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Salbutamol\",\"childId\":" + childId + ",\"dosage\":\"1 puff\",\"time\":\"08:30\",\"status\":\"PENDING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.time").value("08:30"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        Number idValue = JsonPath.<Number>read(response, "$.id");
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
    void renamingChildUpdatesMedicationDisplayNameWithoutChangingItsChildId() throws Exception {
        long childId = createChildNamed("Lina");
        long medicationId = createMedicationForChild(childId);

        mockMvc.perform(put("/api/children/{id}", childId).header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Lina Updated\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/medications").header("X-User-Role", "PARENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + medicationId + ")].childName").value("Lina Updated"));
    }

    @Test
    void deletingChildDoesNotRemoveAnotherChildsMedicationWithTheSameName() throws Exception {
        long firstChildId = createChildNamed("Sam");
        long secondChildId = createChildNamed("Sam");

        createMedicationForChild(firstChildId);
        long secondMedicationId = createMedicationForChild(secondChildId);

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

    private long createMedicationForChild(long childId) throws Exception {
        String response = mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + childId + ",\"dosage\":\"5 drops\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(response, "$.id").longValue();
    }

    @Test
    void parentCanCreateButNotUpdateOrDeleteMedication() throws Exception {
        long childId = createChildNamed("Emma");
        String response = mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Salbutamol\",\"childId\":" + childId + ",\"dosage\":\"1 puff\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number idValue = JsonPath.<Number>read(response, "$.id");
        long id = idValue.longValue();

        mockMvc.perform(put("/api/medications/{id}", id).header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"TAKEN\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/medications/{id}", id).header("X-User-Role", "PARENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void medicationRequiresChildId() throws Exception {
        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Salbutamol\",\"childName\":\"Emma\",\"dosage\":\"1 puff\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void parentAndStaffCanSaveMedicationSchedules() throws Exception {
        long childId = createChildNamed("Schedule Child");

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + childId
                        + ",\"dosage\":\"5 drops\",\"time\":\"08:00\","
                        + "\"frequency\":\"WEEKDAYS_ONLY\",\"startDate\":\"2026-06-23\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.frequency").value("WEEKDAYS_ONLY"))
                .andExpect(jsonPath("$.startDate").value("2026-06-23"));

        mockMvc.perform(post("/api/medications").header("X-User-Role", "STAFF")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Inhaler\",\"childId\":" + childId
                        + ",\"dosage\":\"1 puff\",\"time\":\"10:00\","
                        + "\"frequency\":\"EVERY_X_DAYS\",\"intervalDays\":3,\"startDate\":\"2026-06-23\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.frequency").value("EVERY_X_DAYS"))
                .andExpect(jsonPath("$.intervalDays").value(3));
    }
}
