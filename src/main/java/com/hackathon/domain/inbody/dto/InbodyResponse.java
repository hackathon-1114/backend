package com.hackathon.domain.inbody.dto;

import com.hackathon.domain.inbody.entity.InbodyRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record InbodyResponse(
        Long id,
        LocalDate measuredAt,
        @Schema(description = "photo / manual", example = "photo")
        String source,
        Double weightKg,
        Double skeletalMuscleKg,
        Double bodyFatPercent,
        Double rightArmKg,
        Double leftArmKg,
        Double trunkKg,
        Double rightLegKg,
        Double leftLegKg,
        @Schema(description = "좌우 팔 근육량 차이(%). 큰 쪽 기준. 값이 없으면 null", example = "3.3")
        Double armDiffPct,
        @Schema(description = "좌우 다리 근육량 차이(%). 큰 쪽 기준. 값이 없으면 null", example = "7.5")
        Double legDiffPct,
        @Schema(description = "근육량이 적은 쪽 다리. left / right. 차이가 없거나 값이 없으면 null", example = "left")
        String weakerLeg) {

    public static InbodyResponse from(InbodyRecord r) {
        return new InbodyResponse(r.getId(), r.getMeasuredAt(), r.getSource(), r.getWeightKg(), r.getSkeletalMuscleKg(),
                r.getBodyFatPercent(), r.getRightArmKg(), r.getLeftArmKg(), r.getTrunkKg(), r.getRightLegKg(),
                r.getLeftLegKg(), diffPct(r.getRightArmKg(), r.getLeftArmKg()), diffPct(r.getRightLegKg(), r.getLeftLegKg()),
                weaker(r.getRightLegKg(), r.getLeftLegKg()));
    }

    static Double diffPct(Double right, Double left) {
        if (right == null || left == null || Math.max(right, left) <= 0) return null;
        return Math.round(Math.abs(right - left) / Math.max(right, left) * 1000) / 10.0;
    }

    static String weaker(Double right, Double left) {
        if (right == null || left == null || right.equals(left)) return null;
        return right < left ? "right" : "left";
    }
}
