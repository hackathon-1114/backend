package com.hackathon.domain.insight.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.domain.ai.AiDto;
import com.hackathon.domain.ai.AiService;
import com.hackathon.domain.exerciseset.service.ExerciseSetService;
import com.hackathon.domain.inbody.service.InbodyService;
import com.hackathon.domain.survey.service.SurveyService;
import com.hackathon.domain.formcheck.entity.FormCheck;
import com.hackathon.domain.formcheck.repository.FormCheckRepository;
import com.hackathon.domain.insight.dto.InsightResponse;
import com.hackathon.domain.insight.entity.Insight;
import com.hackathon.domain.insight.repository.InsightRepository;
import com.hackathon.domain.workout.entity.Workout;
import com.hackathon.domain.workout.repository.WorkoutRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** 실제 AI 호출 없이, 기록이 프롬프트로 바뀌는 모양과 응답이 저장되는 흐름만 확인한다. */
class InsightServiceTest {

    private final WorkoutRepository workoutRepository = mock(WorkoutRepository.class);
    private final InsightRepository insightRepository = mock(InsightRepository.class);
    private final FormCheckRepository formCheckRepository = mock(FormCheckRepository.class);
    private final ExerciseSetService exerciseSetService = mock(ExerciseSetService.class);
    private final InbodyService inbodyService = mock(InbodyService.class);
    private final SurveyService surveyService = mock(SurveyService.class);
    private final AiService aiService = mock(AiService.class);
    private final InsightService service = new InsightService(workoutRepository, insightRepository, formCheckRepository,
            exerciseSetService, inbodyService, surveyService, aiService);

    @Test
    void describeAddsWeekdayAndGapSoTheModelDoesNotHaveToCompute() {
        String text = InsightService.describe(List.of(
                workout(LocalDateTime.of(2026, 11, 8, 8, 0), 13.0, "최장 거리"),
                workout(LocalDateTime.of(2026, 11, 12, 19, 50), 4.0, null)),
                List.of(FormCheck.builder().userId(1L).exercise("스쿼트").performedAt(LocalDateTime.of(2026, 11, 4, 22, 0))
                                .score(58).issueLabels("무릎 안쪽 모임, 깊이 부족").build(),
                        FormCheck.builder().userId(1L).exercise("러닝").performedAt(LocalDateTime.of(2026, 10, 31, 7, 0))
                                .score(86).issueLabels("").build()));

        assertThat(text).contains("2026-11-08(일) 08:00 | 러닝 | 40분 | 13.0km | 강도 3/5 | 기분 4/5 | 메모: 최장 거리");
        assertThat(text).contains("2026-11-12(목) 19:50 | 러닝 | 40분 | 4.0km | 강도 3/5 | 기분 4/5 | 직전 기록 후 4일\n");
        assertThat(text).contains("2026-11-04(수) 22:00 | 스쿼트 | 자세 점수 58/100 | 지적: 무릎 안쪽 모임, 깊이 부족\n");
        assertThat(text).contains("2026-10-31(토) 07:00 | 러닝 | 자세 점수 86/100 | 지적: 없음\n");
    }

    @Test
    void analyzeSendsRecordsOldestFirstAndReplacesPreviousInsights() throws Exception {
        List<Workout> newestFirst = new ArrayList<>();
        for (int day = 10; day >= 1; day--) {
            newestFirst.add(workout(LocalDateTime.of(2026, 11, day, 7, 0), 5.0, null));
        }
        List<Insight> previous = List.of(Insight.builder().userId(1L).title("이전 결과").build());
        when(workoutRepository.findByUserIdOrderByPerformedAtDesc(1L)).thenReturn(newestFirst);
        when(exerciseSetService.describeHistory(1L, InsightService.MAX_SETS)).thenReturn("\n세트 기록 줄\n");
        when(inbodyService.describeLatest(1L)).thenReturn("\n인바디 줄\n");
        when(surveyService.describeLatest(1L)).thenReturn("\n문진 줄\n");
        when(insightRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(previous);
        when(insightRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(aiService.json(any())).thenReturn(new ObjectMapper().readTree("""
                {"patterns":[
                  {"title":"아침에 뛴 날 기분이 더 좋습니다","evidence":"11월 1일~10일 07시 기록","suggestion":"이번 주는 아침에 뛰어 보세요"},
                  {"title":"","evidence":"빈 제목은 버린다","suggestion":""}]}
                """));

        List<InsightResponse> result = service.analyze(1L);

        assertThat(result).extracting(InsightResponse::title).containsExactly("아침에 뛴 날 기분이 더 좋습니다");
        verify(insightRepository).deleteAll(previous);

        ArgumentCaptor<AiDto.JsonRequest> sent = ArgumentCaptor.forClass(AiDto.JsonRequest.class);
        verify(aiService).json(sent.capture());
        String message = sent.getValue().message();
        assertThat(message.indexOf("2026-11-01")).isLessThan(message.indexOf("2026-11-10"));
        assertThat(message).contains("세트 기록 줄", "인바디 줄", "문진 줄");
    }

    private static Workout workout(LocalDateTime at, Double km, String memo) {
        return Workout.builder().userId(1L).type("러닝").performedAt(at).durationMinutes(40)
                .distanceKm(km).intensity(3).mood(4).memo(memo).build();
    }
}
