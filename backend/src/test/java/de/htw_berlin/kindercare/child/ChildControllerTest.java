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
        String response = mockMvc.perform(post("/api/children").header("X-User-Role", "PARENT")
                .contentType(JSON_CONTENT_TYPE)
                .content("{\"name\":\"Emma\",\"allergies\":\"Peanuts\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Emma"))
                .andReturn().getResponse().getContentAsString();

        long id = responseId(response);
        mockMvc.perform(get("/api/children").header("X-User-Role", "STAFF"))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/children/{id}", id)
                .header("X-User-Role", "PARENT").contentType(JSON_CONTENT_TYPE)
                .content("{\"allergies\":\"Peanuts, Milk\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergies").value("Peanuts, Milk"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/children/{id}", id)
                .header("X-User-Role", "PARENT"))
                .andExpect(status().isNoContent());
    }

    private long responseId(String response) {
        Object value = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new AssertionError("Expected a numeric id in the response.");
    }
}
