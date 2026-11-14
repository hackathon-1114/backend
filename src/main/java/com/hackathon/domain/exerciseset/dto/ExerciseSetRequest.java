package com.hackathon.domain.exerciseset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class ExerciseSetRequest {

    /** reps는 /pose-test.html의 window.poseTest.reps 를 그대로 보내면 된다 */
    public record Create(
            @Schema(description = "운동 종목", example = "스쿼트")
            @NotBlank @Size(max = 30) String exercise,
            @Schema(description = "세트를 한 시각 (한국 시간). 생략하면 지금", example = "2026-11-14T19:30:00")
            LocalDateTime performedAt,
            @Schema(description = "반복별 측정값. 한 세트 1~100회")
            @NotEmpty @Size(max = 100) List<@Valid Rep> reps,
            @Schema(description = "강화하려는 근육 (선택). muscles의 name 중 하나", example = "대둔근")
            @Size(max = 30) String targetMuscle,
            @Schema(description = "근육별 근전도 RMS (선택). 센서가 없으면 시뮬레이션 값을 보내고 emgSimulated를 true로 둔다")
            @Size(max = 12) List<@Valid Muscle> muscles,
            @Schema(description = "근전도 값이 시뮬레이션이면 true", example = "true")
            Boolean emgSimulated) {
    }

    public record Rep(
            @Schema(description = "촬영 방향", example = "side")
            @Pattern(regexp = "side|front") String view,
            @Schema(description = "최저점의 엉덩이 높이 비율. 서 있을 때 1, 낮을수록 깊이 앉은 것", example = "0.52")
            @NotNull @DecimalMin("0.0") @DecimalMax("1.2") Double depth,
            @Schema(description = "한 번에 걸린 시간(ms)", example = "2300")
            @NotNull @Min(100) @Max(60000) Integer durationMs,
            @Schema(description = "상체 최대 기울기(도). 측면에서만 의미 있음", example = "32.5")
            Double maxLean,
            @Schema(description = "최저점 허벅지 각도(도). 수평이 0, 서 있으면 90, 음수면 수평보다 깊음. 측면에서만 의미 있음", example = "8.0")
            Double thighAngle,
            @Schema(description = "최저점 무릎 간격 / 발목 간격. 정면에서만 값이 있음", example = "1.25")
            Double kneeRatio,
            @Schema(description = "골반 중심이 발 중심에서 벗어난 정도 / 발목 간격. 정면에서만 값이 있음", example = "0.04")
            Double shift,
            @Schema(description = "브라우저 규칙이 붙인 관찰", example = "[\"깊이 부족\"]")
            @Size(max = 6) List<@Size(max = 30) String> findings) {
    }

    public record Muscle(
            @Schema(description = "근육 이름", example = "대둔근")
            @NotBlank @Size(max = 30) String name,
            @Schema(description = "left / right / center", example = "left")
            @NotBlank @Pattern(regexp = "left|right|center") String side,
            @Schema(description = "반복별 RMS. reps와 같은 순서", example = "[0.41, 0.39, 0.36]")
            @NotEmpty @Size(max = 100) List<@NotNull @PositiveOrZero Double> rmsPerRep) {
    }
}
