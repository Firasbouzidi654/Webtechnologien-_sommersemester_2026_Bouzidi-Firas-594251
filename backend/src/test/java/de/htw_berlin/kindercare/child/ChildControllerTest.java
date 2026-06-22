package de.htw_berlin.kindercare.child;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChildControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void parentCanCreateUpdateAndDeleteChild() throws Exception {
        String response = mockMvc.perform(post("/api/children").header("X-User-Role", "PARENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Emma\",\"allergies\":\"Peanuts\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Emma"))
                .andReturn().getResponse().getContentAsString();

        Number idValue = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        long id = idValue.longValue();
        mockMvc.perform(get("/api/children").header("X-User-Role", "STAFF"))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/children/{id}", id)
                .header("X-User-Role", "PARENT").contentType(MediaType.APPLICATION_JSON)
                .content("{\"allergies\":\"Peanuts, Milk\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergies").value("Peanuts, Milk"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/children/{id}", id)
                .header("X-User-Role", "PARENT"))
                .andExpect(status().isNoContent());
    }
}
