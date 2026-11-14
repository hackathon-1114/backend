package com.hackathon.domain.formcheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record FormCheckResponse(
        Long id,
        String exercise,
        LocalDateTime performedAt,
        @Schema(description = "자세 점수 0~100", example = "72")
        Integer score,
        @Schema(description = "한두 문장 총평")
        String summary,
        @Schema(description = "잘한 점")
        List<String> goodPoints,
        @Schema(description = "지적 사항. 문제가 안 보이면 빈 배열")
        List<Issue> issues,
        String memo) {

    public record Issue(
            @Schema(description = "지적 이름", example = "무릎 안쪽 모임")
            String label,
            @Schema(description = "신체 부위", example = "무릎")
            String bodyPart,
            @Schema(description = "몇 번째 장면에서 무엇이 보였는지", example = "4번째 장면에서 일어설 때 양 무릎이 발끝보다 안쪽으로 들어옵니다")
            String observation,
            @Schema(description = "다음 세트에 바로 써먹을 교정 한 문장", example = "무릎을 새끼발가락 쪽으로 밀면서 일어나세요")
            String cue,
            @Schema(description = "낮음 / 보통 / 높음", example = "보통")
            String severity) {
    }
}
