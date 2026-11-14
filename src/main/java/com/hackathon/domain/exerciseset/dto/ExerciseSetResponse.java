package com.hackathon.domain.exerciseset.dto;

import com.hackathon.domain.exerciseset.service.SetAnalyzer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record ExerciseSetResponse(
        Long id,
        String exercise,
        LocalDateTime performedAt,
        @Schema(description = "서버가 반복별 수치에서 계산한 세트 통계. AI를 쓰지 않아 바로 나온다")
        SetAnalyzer.Stats stats,
        @Schema(description = "근전도 요약. muscles를 보내지 않았으면 null")
        SetAnalyzer.Emg emg,
        List<ExerciseSetRequest.Rep> reps,
        @Schema(description = "AI 피드백. POST /api/sets/{id}/feedback 를 부르기 전에는 null")
        Feedback feedback) {

    public record Feedback(
            @Schema(description = "세트 총평 한두 문장")
            String summary,
            List<Observation> observations,
            @Schema(description = "다음 세트에서 해볼 행동")
            List<String> suggestions,
            @Schema(description = "이 사람의 측정 결과에 맞춘 보완 운동 2~3개")
            List<Exercise> exercises,
            @Schema(description = "다음 측정에서 달라졌는지 확인할 것")
            List<String> retestGoals) {
    }

    public record Exercise(
            @Schema(description = "운동 이름", example = "고블릿 스쿼트")
            String name,
            @Schema(description = "이 사람에게 권하는 이유. 측정 수치를 근거로 든다")
            String reason,
            @Schema(description = "방법 한두 문장")
            String how) {
    }

    public record Observation(
            @Schema(description = "관찰 한 줄", example = "7회째부터 깊이가 얕아졌습니다")
            String title,
            @Schema(description = "근거가 된 수치")
            String evidence,
            @Schema(description = "이 움직임이 이어질 때 부담이 실릴 수 있는 부위. 가능성으로만 표현")
            String possibleLoad) {
    }
}
