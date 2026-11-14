package com.hackathon.domain.workout.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class WorkoutRequest {

    public record Create(
            @Schema(description = "운동 종류", example = "러닝")
            @NotBlank @Size(max = 30) String type,
            @Schema(description = "운동 시작 시각 (한국 시간). 생략하면 지금", example = "2026-11-14T07:30:00")
            LocalDateTime performedAt,
            @Schema(description = "운동 시간(분)", example = "40")
            @NotNull @Min(1) @Max(1440) Integer durationMinutes,
            @Schema(description = "거리(km). 거리가 없는 운동은 생략", example = "6.2")
            @PositiveOrZero Double distanceKm,
            @Schema(description = "체감 강도 1(가벼움)~5(한계)", example = "3")
            @Min(1) @Max(5) Integer intensity,
            @Schema(description = "운동 후 기분 1(나쁨)~5(좋음)", example = "4")
            @Min(1) @Max(5) Integer mood,
            @Schema(description = "한 줄 메모", example = "퇴근 후 한강, 다리가 가벼웠음")
            @Size(max = 500) String memo) {
    }

    /** null인 필드는 수정 안 함 */
    public record Update(
            @Size(max = 30) String type,
            LocalDateTime performedAt,
            @Min(1) @Max(1440) Integer durationMinutes,
            @PositiveOrZero Double distanceKm,
            @Min(1) @Max(5) Integer intensity,
            @Min(1) @Max(5) Integer mood,
            @Size(max = 500) String memo) {
    }
}
