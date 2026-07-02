package de.htw_berlin.kindercare.medication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MedicationControllerTest {
    private static final String JSON_CONTENT_TYPE = "application/json";

    @Autowired MockMvc mockMvc;
    @Autowired MedicationRepository medications;

    private record TestChild(long id, long parentId) { }

    @Test
    void staffCanCreateUpdateAndDeleteMedication() throws Exception {
        long childId = createChildNamed("Emma");
        String response = mockMvc.perform(post("/api/medications").header("X-User-Role", "STAFF")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Salbutamol\",\"childId\":" + childId + ",\"dosage\":\"1 puff\",\"time\":\"08:30\",\"scheduledDate\":\"2026-07-03\",\"status\":\"PENDING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.time").value("08:30"))
                .andExpect(jsonPath("$.scheduledDate").value("2026-07-03"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        long id = responseId(response);

        mockMvc.perform(get("/api/medications").header("X-User-Role", "STAFF"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/medications/{id}", id).header("X-User-Role", "STAFF")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"time\":\"09:00\",\"status\":\"TAKEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").value("09:00"))
                .andExpect(jsonPath("$.status").value("TAKEN"));

        mockMvc.perform(delete("/api/medications/{id}", id).header("X-User-Role", "STAFF"))
                .andExpect(status().isNoContent());
    }

    @Test
    void medicationLinksToChildByIdNotJustByName() throws Exception {
        TestChild child = createOwnedChildNamed("Lina");

        // The request sends a stale/wrong childName on purpose; the backend must
        // ignore it and use the real name from the child record via childId.
        String medicationResponse = mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + child.id() + ",\"childName\":\"Wrong Name\",\"dosage\":\"5 drops\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.childId").value(child.id()))
                .andExpect(jsonPath("$.childName").value("Lina"))
                .andReturn().getResponse().getContentAsString();

        long medicationId = responseId(medicationResponse);

        mockMvc.perform(delete("/api/children/{id}", child.id()).header("X-User-Role", "PARENT").header("X-User-Id", child.parentId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/medications").header("X-User-Role", "PARENT").header("X-User-Id", child.parentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + medicationId + ")]").isEmpty());
    }

    @Test
    void renamingChildUpdatesMedicationDisplayNameWithoutChangingItsChildId() throws Exception {
        TestChild child = createOwnedChildNamed("Lina");
        long medicationId = createMedicationForChild(child);

        mockMvc.perform(put("/api/children/{id}", child.id()).header("X-User-Role", "PARENT").header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Lina Updated\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/medications").header("X-User-Role", "PARENT").header("X-User-Id", child.parentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + medicationId + ")].childName").value("Lina Updated"));
    }

    @Test
    void deletingChildDoesNotRemoveAnotherChildsMedicationWithTheSameName() throws Exception {
        TestChild firstChild = createOwnedChildNamed("Sam");
        TestChild secondChild = createOwnedChildNamed("Sam");

        createMedicationForChild(firstChild);
        long secondMedicationId = createMedicationForChild(secondChild);

        mockMvc.perform(delete("/api/children/{id}", firstChild.id()).header("X-User-Role", "PARENT").header("X-User-Id", firstChild.parentId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/medications").header("X-User-Role", "STAFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + secondMedicationId + ")]").isNotEmpty());
    }

    @Test
    void parentCannotSeeOrCreateMedicationForAnotherParentsChild() throws Exception {
        TestChild firstChild = createOwnedChildNamed("First Medication Child");
        TestChild secondChild = createOwnedChildNamed("Second Medication Child");
        long secondMedicationId = createMedicationForChild(secondChild);

        mockMvc.perform(get("/api/medications").header("X-User-Role", "PARENT").header("X-User-Id", firstChild.parentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + secondMedicationId + ")]").isEmpty());

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", firstChild.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Blocked\",\"childId\":" + secondChild.id() + ",\"dosage\":\"1 ml\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\"}"))
                .andExpect(status().isNotFound());
    }

    private long createChildNamed(String name) throws Exception {
        return createOwnedChildNamed(name).id();
    }

    private TestChild createOwnedChildNamed(String name) throws Exception {
        long parentId = createParentAccount();
        String response = mockMvc.perform(post("/api/children").header("X-User-Role", "PARENT")
                .header("X-User-Id", parentId)
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"" + name + "\",\"allergies\":\"\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new TestChild(responseId(response), parentId);
    }

    private long createMedicationForChild(TestChild child) throws Exception {
        String response = mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + child.id() + ",\"dosage\":\"5 drops\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return responseId(response);
    }

    @Test
    void parentCanCreateButNotUpdateOrDeleteMedication() throws Exception {
        TestChild child = createOwnedChildNamed("Emma");
        String response = mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Salbutamol\",\"childId\":" + child.id() + ",\"dosage\":\"1 puff\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = responseId(response);

        mockMvc.perform(put("/api/medications/{id}", id).header("X-User-Role", "PARENT")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"status\":\"TAKEN\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/medications/{id}", id).header("X-User-Role", "PARENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void medicationRequiresChildId() throws Exception {
        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Salbutamol\",\"childName\":\"Emma\",\"dosage\":\"1 puff\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void medicationRejectsBlankNameOrDosage() throws Exception {
        TestChild child = createOwnedChildNamed("Validation Child");

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\" \",\"childId\":" + child.id() + ",\"dosage\":\"5 drops\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + child.id() + ",\"dosage\":\" \",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void medicationCreationRequiresAllowedRole() throws Exception {
        long childId = createChildNamed("Role Child");

        mockMvc.perform(post("/api/medications")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + childId + ",\"dosage\":\"5 drops\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void medicationRejectsInvalidStatusAndTime() throws Exception {
        TestChild child = createOwnedChildNamed("Invalid Medication Child");

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + child.id() + ",\"dosage\":\"5 drops\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\",\"status\":\"DONE\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + child.id() + ",\"dosage\":\"5 drops\",\"scheduledDate\":\"2026-07-01\",\"time\":\"25:99\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + child.id() + ",\"dosage\":\"5 drops\",\"scheduledDate\":\"2026-07-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void medicationReturnsNotFoundForUnknownChild() throws Exception {
        long parentId = createParentAccount();
        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", parentId)
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":999999,\"dosage\":\"5 drops\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void medicationRequiresValidScheduledDate() throws Exception {
        TestChild child = createOwnedChildNamed("Date Validation Child");

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + child.id() + ",\"dosage\":\"5 drops\",\"time\":\"12:00\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + child.id()
                        + ",\"dosage\":\"5 drops\",\"time\":\"12:00\",\"scheduledDate\":\"not-a-date\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void medicationTasksOnDifferentDatesKeepIndependentStatus() throws Exception {
        TestChild child = createOwnedChildNamed("Task Child");

        String firstResponse = mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Paracetamol\",\"childId\":" + child.id()
                        + ",\"dosage\":\"10 ml\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-01\",\"status\":\"TAKEN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduledDate").value("2026-07-01"))
                .andExpect(jsonPath("$.status").value("TAKEN"))
                .andReturn().getResponse().getContentAsString();

        long firstId = responseId(firstResponse);

        String secondResponse = mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Paracetamol\",\"childId\":" + child.id()
                        + ",\"dosage\":\"10 ml\",\"time\":\"12:00\",\"scheduledDate\":\"2026-07-08\",\"status\":\"PENDING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduledDate").value("2026-07-08"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        long secondId = responseId(secondResponse);

        mockMvc.perform(put("/api/medications/{id}", firstId).header("X-User-Role", "STAFF")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"status\":\"MISSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MISSED"));

        mockMvc.perform(get("/api/medications").header("X-User-Role", "STAFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + firstId + ")].status").value("MISSED"))
                .andExpect(jsonPath("$[?(@.id == " + secondId + ")].status").value("PENDING"));
    }

    @Test
    void overduePendingMedicationIsMarkedMissedWhenCreated() throws Exception {
        TestChild child = createOwnedChildNamed("Overdue Create Child");

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + child.id()
                        + ",\"dosage\":\"5 drops\",\"time\":\"08:00\",\"scheduledDate\":\"2026-07-02\",\"status\":\"PENDING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("MISSED"));
    }

    @Test
    void futurePendingMedicationStaysPending() throws Exception {
        TestChild child = createOwnedChildNamed("Future Pending Child");

        mockMvc.perform(post("/api/medications").header("X-User-Role", "PARENT")
                .header("X-User-Id", child.parentId())
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Vitamin D\",\"childId\":" + child.id()
                        + ",\"dosage\":\"5 drops\",\"time\":\"13:00\",\"scheduledDate\":\"2026-07-02\",\"status\":\"PENDING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void existingOverduePendingMedicationIsMarkedMissedWhenRead() throws Exception {
        long childId = createChildNamed("Overdue Read Child");

        Medication medication = new Medication("Vitamin D", "Overdue Read Child", "5 drops", "08:00", "PENDING", "2026-07-01");
        medication.setChildId(childId);
        long medicationId = medications.save(medication).getId();

        mockMvc.perform(get("/api/medications").header("X-User-Role", "STAFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + medicationId + ")].status").value("MISSED"));

        mockMvc.perform(get("/api/medications").header("X-User-Role", "STAFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + medicationId + ")].status").value("MISSED"));
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-07-02T12:00:00Z"), ZoneId.of("UTC"));
        }
    }

    private long responseId(String response) {
        Object value = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new AssertionError("Expected a numeric id in the response.");
    }

    private long createParentAccount() throws Exception {
        String email = "parent-" + java.util.UUID.randomUUID() + "@example.test";
        String response = mockMvc.perform(post("/api/auth/register")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"email\":\"" + email + "\",\"password\":\"SecurePass123\",\"role\":\"PARENT\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return responseId(response);
    }
}
