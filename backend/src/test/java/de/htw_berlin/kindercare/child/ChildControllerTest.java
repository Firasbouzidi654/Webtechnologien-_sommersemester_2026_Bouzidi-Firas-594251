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
    void createsAndListsChildren() throws Exception {
        mockMvc.perform(post("/api/children").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Emma\",\"allergies\":\"Peanuts\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Emma"));

        mockMvc.perform(get("/api/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Emma"));
    }
}
