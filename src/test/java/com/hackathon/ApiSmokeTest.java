package com.hackathon;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "anthropic.api-key=")
@AutoConfigureMockMvc
class ApiSmokeTest {

    @Autowired
    MockMvc mvc;

    @Test
    void health() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("UP"));
    }

    @Test
    void sampleCrudAndSeed() throws Exception {
        mvc.perform(get("/api/samples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        mvc.perform(post("/api/samples").header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"content\":\"c\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(7))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    void validationAndNotFound() throws Exception {
        mvc.perform(post("/api/samples").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"))
                .andExpect(jsonPath("$.data.title").exists());

        mvc.perform(get("/api/samples/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SAMPLE404"));
    }

    @Test
    void aiWithoutKeyReturns503() throws Exception {
        mvc.perform(post("/api/ai/chat").contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"hi\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI503"));
    }

    @Test
    void aiJsonValidatesSchema() throws Exception {
        mvc.perform(post("/api/ai/json").contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"hi\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.schema").exists());
    }

    @Test
    void swaggerDocs() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
