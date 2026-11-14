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
    void setsAreSavedWithStatsAndSeeded() throws Exception {
        mvc.perform(get("/api/sets").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].stats.repCount").value(10))
                .andExpect(jsonPath("$.data[0].stats.fadeFromRep").value(5))
                .andExpect(jsonPath("$.data[1].stats.fadeFromRep").doesNotExist())
                .andExpect(jsonPath("$.data[2].stats.fadeFromRep").value(6));

        String reps = "[" + "{\"view\":\"side\",\"depth\":0.5,\"durationMs\":2000,\"maxLean\":30,\"thighAngle\":5,\"findings\":[]},".repeat(3)
                + "{\"view\":\"side\",\"depth\":0.62,\"durationMs\":3000,\"maxLean\":41,\"thighAngle\":22,\"minTorso\":0.9,\"findings\":[\"깊이 부족\"]}]";
        mvc.perform(post("/api/sets").header("X-User-Id", "8").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exercise\":\"스쿼트\",\"reps\":" + reps + ",\"targetMuscle\":\"대둔근\",\"emgSimulated\":true,"
                                + "\"muscles\":[{\"name\":\"대둔근\",\"side\":\"left\",\"rmsPerRep\":[0.3,0.3,0.3,0.2]},"
                                + "{\"name\":\"대둔근\",\"side\":\"right\",\"rmsPerRep\":[0.4,0.4,0.4,0.4]}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stats.repCount").value(4))
                .andExpect(jsonPath("$.data.stats.fadeFromRep").value(4))
                .andExpect(jsonPath("$.data.stats.findingCounts['깊이 부족']").value(1))
                .andExpect(jsonPath("$.data.emg.simulated").value(true))
                .andExpect(jsonPath("$.data.emg.asymmetries[0].stronger").value("right"))
                .andExpect(jsonPath("$.data.feedback").doesNotExist());

        mvc.perform(post("/api/sets").contentType(MediaType.APPLICATION_JSON).content("{\"exercise\":\"스쿼트\",\"reps\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.reps").exists());

        // 피드백은 AI를 부른다. 테스트에는 키가 없으므로 503이고, 세트는 그대로 남는다
        mvc.perform(post("/api/sets/1/feedback"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI503"));
        mvc.perform(get("/api/sets/9999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SET404"));
    }

    @Test
    void inbodyManualEntryPhotoAndSeed() throws Exception {
        mvc.perform(get("/api/inbody").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].source").value("sample"))
                .andExpect(jsonPath("$.data[0].legDiffPct").value(7.5))
                .andExpect(jsonPath("$.data[0].weakerLeg").value("left"));

        mvc.perform(post("/api/inbody").header("X-User-Id", "9").contentType(MediaType.APPLICATION_JSON).content("{\"sample\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.source").value("sample"))
                .andExpect(jsonPath("$.data.leftLegKg").value(8.6));

        mvc.perform(post("/api/inbody").header("X-User-Id", "8").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"values\":{\"weightKg\":70,\"rightLegKg\":9.0,\"leftLegKg\":10.0},\"measuredAt\":\"2026-11-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.source").value("manual"))
                .andExpect(jsonPath("$.data.legDiffPct").value(10.0))
                .andExpect(jsonPath("$.data.weakerLeg").value("right"))
                .andExpect(jsonPath("$.data.armDiffPct").doesNotExist());

        mvc.perform(post("/api/inbody").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INBODY400"));

        mvc.perform(post("/api/inbody").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"image\":{\"mediaType\":\"image/jpeg\",\"data\":\"AAAA\"}}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI503"));
    }

    @Test
    void surveyLatestWins() throws Exception {
        mvc.perform(get("/api/surveys/latest").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.discomforts[0].bodyPart").value("왼쪽 무릎"));

        mvc.perform(get("/api/surveys/latest").header("X-User-Id", "55"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SURVEY404"));

        mvc.perform(post("/api/surveys").header("X-User-Id", "8").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goal\":\"체력\",\"discomforts\":[{\"bodyPart\":\"허리\",\"level\":11}]}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/surveys").header("X-User-Id", "8").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goal\":\"체력\",\"discomforts\":[{\"bodyPart\":\"허리\",\"situation\":\"오래 앉아 있을 때\",\"level\":4}]}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/surveys/latest").header("X-User-Id", "8"))
                .andExpect(jsonPath("$.data.goal").value("체력"))
                .andExpect(jsonPath("$.data.discomforts[0].level").value(4));
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
