package com.hackathon.domain.inbody.dto;

import com.hackathon.domain.ai.AiDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class InbodyRequest {

    /** image와 values 중 하나는 있어야 한다. 둘 다 있으면 values를 쓴다 */
    public record Create(
            @Schema(description = "인바디 결과지 사진. AI가 수치를 읽는다 (5~15초)")
            AiDto.@Valid Image image,
            @Schema(description = "직접 입력한 수치. 사진 대신 쓰거나, 사진에서 읽은 값을 고쳐서 다시 저장할 때 쓴다")
            @Valid Values values,
            @Schema(description = "측정일. 생략하면 결과지에서 읽고, 못 읽으면 오늘", example = "2026-11-10")
            LocalDate measuredAt) {
    }

    public record Values(
            @Schema(description = "체중(kg)", example = "68.4") @Positive @Max(300) Double weightKg,
            @Schema(description = "골격근량(kg)", example = "30.1") @Positive @Max(100) Double skeletalMuscleKg,
            @Schema(description = "체지방률(%)", example = "18.5") @Positive @Max(80) Double bodyFatPercent,
            @Schema(description = "부위별 근육량, 오른팔(kg)", example = "3.0") @Positive @Max(20) Double rightArmKg,
            @Schema(description = "부위별 근육량, 왼팔(kg)", example = "2.9") @Positive @Max(20) Double leftArmKg,
            @Schema(description = "부위별 근육량, 몸통(kg)", example = "24.2") @Positive @Max(80) Double trunkKg,
            @Schema(description = "부위별 근육량, 오른다리(kg)", example = "9.3") @Positive @Max(40) Double rightLegKg,
            @Schema(description = "부위별 근육량, 왼다리(kg)", example = "8.6") @Positive @Max(40) Double leftLegKg) {
    }
}
