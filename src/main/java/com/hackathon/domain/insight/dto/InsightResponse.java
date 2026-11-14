package com.hackathon.domain.insight.dto;

import com.hackathon.domain.insight.entity.Insight;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record InsightResponse(
        Long id,
        @Schema(description = "패턴 한 줄 요약", example = "장거리를 뛴 뒤에는 나흘 넘게 쉽니다")
        String title,
        @Schema(description = "근거가 된 기록의 날짜와 수치")
        String evidence,
        @Schema(description = "이번 주에 해볼 행동 한 가지")
        String suggestion,
        LocalDateTime createdAt) {

    public static InsightResponse from(Insight i) {
        return new InsightResponse(i.getId(), i.getTitle(), i.getEvidence(), i.getSuggestion(), i.getCreatedAt());
    }
}
