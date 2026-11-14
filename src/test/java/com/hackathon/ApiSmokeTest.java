package com.hackathon;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void workoutCrudAndSeed() throws Exception {
        mvc.perform(get("/api/workouts").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(15))
                .andExpect(jsonPath("$.data[0].type").value("러닝"))
                .andExpect(jsonPath("$.data[0].distanceKm").value(13.0));

        mvc.perform(post("/api/workouts").header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"수영\",\"durationMinutes\":40,\"mood\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(7))
                .andExpect(jsonPath("$.data.performedAt").exists());

        mvc.perform(post("/api/workouts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"러닝\",\"durationMinutes\":0,\"mood\":9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.durationMinutes").exists())
                .andExpect(jsonPath("$.data.mood").exists());

        mvc.perform(get("/api/workouts/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WORKOUT404"));
    }

    @Test
    void insightsNeedEnoughRecordsAndAnAiKey() throws Exception {
        mvc.perform(post("/api/insights/analyze").header("X-User-Id", "99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSIGHT400"));

        // 시드 유저는 기록이 충분해 AI 호출까지 간다. 테스트에는 키가 없으므로 503
        mvc.perform(post("/api/insights/analyze").header("X-User-Id", "1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI503"));

        mvc.perform(get("/api/insights").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void formChecksSeedValidationAndAiKey() throws Exception {
        mvc.perform(get("/api/form-checks").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].exercise").value("스쿼트"))
                .andExpect(jsonPath("$.data[0].score").value(58))
                .andExpect(jsonPath("$.data[0].issues[0].label").value("무릎 안쪽 모임"))
                .andExpect(jsonPath("$.data[0].goodPoints[0]").value("허리는 중립을 유지합니다"));

        mvc.perform(post("/api/form-checks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exercise\":\"스쿼트\",\"frames\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.frames").exists());

        // 장면이 유효하면 AI 호출까지 간다. 테스트에는 키가 없으므로 503
        mvc.perform(post("/api/form-checks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exercise\":\"스쿼트\",\"frames\":[{\"mediaType\":\"image/jpeg\",\"data\":\"AAAA\"}]}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI503"));

        // 장면 전체 크기가 상한을 넘으면 AI를 부르기 전에 거절한다
        String big = "A".repeat(3_100_000);
        mvc.perform(post("/api/form-checks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exercise\":\"스쿼트\",\"frames\":[{\"mediaType\":\"image/jpeg\",\"data\":\"" + big
                                + "\"},{\"mediaType\":\"image/jpeg\",\"data\":\"" + big + "\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FORMCHECK400"));

        mvc.perform(get("/api/form-checks/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FORMCHECK404"));

        mvc.perform(get("/form-check.html")).andExpect(status().isOk());
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
    void aiRejectsUnsupportedImageType() throws Exception {
        mvc.perform(post("/api/ai/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"이게 뭐야?\",\"images\":[{\"mediaType\":\"application/pdf\",\"data\":\"AAAA\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data['images[0].mediaType']").exists());
    }

    @Test
    void wrongMethodReturns405NotServerError() throws Exception {
        mvc.perform(put("/api/samples"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("COMMON405"));
    }

    @Test
    void swaggerDocs() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
