package de.htw_berlin.kindercare.child;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChildControllerTest {
    private static final String JSON_CONTENT_TYPE = "application/json";

    @Autowired MockMvc mockMvc;

    @Test
    void parentCanCreateUpdateAndDeleteChild() throws Exception {
        long parentId = createParentAccount();
        String response = mockMvc.perform(post("/api/children").header("X-User-Role", "PARENT")
                .header("X-User-Id", parentId)
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Emma\",\"allergies\":\"Peanuts\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Emma"))
                .andReturn().getResponse().getContentAsString();

        long id = responseId(response);
        mockMvc.perform(get("/api/children").header("X-User-Role", "STAFF"))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/children/{id}", id)
                .header("X-User-Role", "PARENT").header("X-User-Id", parentId).contentType(JSON_CONTENT_TYPE)
                .content("{\"allergies\":\"Peanuts, Milk\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergies").value("Peanuts, Milk"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/children/{id}", id)
                .header("X-User-Role", "PARENT").header("X-User-Id", parentId))
                .andExpect(status().isNoContent());
    }

    @Test
    void parentOnlySeesChildrenAssignedToTheirAccount() throws Exception {
        long firstParentId = createParentAccount();
        long secondParentId = createParentAccount();

        createChildNamed("First Child", firstParentId);
        long secondChildId = createChildNamed("Second Child", secondParentId);

        mockMvc.perform(get("/api/children").header("X-User-Role", "PARENT").header("X-User-Id", firstParentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'First Child')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.name == 'Second Child')]").isEmpty());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/children/{id}", secondChildId)
                .header("X-User-Role", "PARENT").header("X-User-Id", firstParentId)
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"allergies\":\"Peanuts\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanViewAllChildrenAcrossParents() throws Exception {
        long firstParentId = createParentAccount();
        long secondParentId = createParentAccount();

        createChildNamed("Admin First Child", firstParentId);
        createChildNamed("Admin Second Child", secondParentId);

        mockMvc.perform(get("/api/children").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Admin First Child')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.name == 'Admin Second Child')]").isNotEmpty());
    }

    @Test
    void childrenListIsPublicForRenderHealthAndApiDemo() throws Exception {
        long parentId = createParentAccount();
        createChildNamed("Public Child", parentId);

        mockMvc.perform(get("/api/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Public Child')]").isNotEmpty());
    }

    @Test
    void newParentSeesNoChildrenByDefault() throws Exception {
        long parentId = createParentAccount();

        mockMvc.perform(get("/api/children").header("X-User-Role", "PARENT").header("X-User-Id", parentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void childRejectsBlankAllergyEntries() throws Exception {
        long parentId = createParentAccount();
        mockMvc.perform(post("/api/children").header("X-User-Role", "PARENT")
                .header("X-User-Id", parentId)
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Emma\",\"allergies\":\"Peanuts, \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void childCreationRequiresParentRole() throws Exception {
        mockMvc.perform(post("/api/children")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Emma\",\"allergies\":\"\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void childCreationRequiresName() throws Exception {
        long parentId = createParentAccount();
        mockMvc.perform(post("/api/children").header("X-User-Role", "PARENT")
                .header("X-User-Id", parentId)
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\" \",\"allergies\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private long createChildNamed(String name, long parentId) throws Exception {
        String response = mockMvc.perform(post("/api/children")
                .header("X-User-Role", "PARENT")
                .header("X-User-Id", parentId)
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"" + name + "\",\"allergies\":\"\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return responseId(response);
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

    private long responseId(String response) {
        Object value = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new AssertionError("Expected a numeric id in the response.");
    }
}
