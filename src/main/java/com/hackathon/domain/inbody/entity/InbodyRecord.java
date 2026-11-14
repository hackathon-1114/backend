package com.hackathon.domain.inbody.entity;

import com.hackathon.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 인바디(체성분) 결과지 한 장의 수치. 읽지 못한 값은 null */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InbodyRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    /** 측정일. 결과지에서 못 읽으면 저장한 날 */
    @Column(nullable = false)
    private LocalDate measuredAt;

    /** sample(서버가 정해 둔 가상 수치) / manual(직접 입력) / photo(사진에서 AI가 읽음) */
    @Column(nullable = false, length = 10)
    private String source;

    private Double weightKg;
    private Double skeletalMuscleKg;
    private Double bodyFatPercent;
    private Double bmi;
    private Double rightArmKg;
    private Double leftArmKg;
    private Double trunkKg;
    private Double rightLegKg;
    private Double leftLegKg;

    @Builder
    public InbodyRecord(Long userId, LocalDate measuredAt, String source, Double weightKg, Double skeletalMuscleKg,
                        Double bodyFatPercent, Double bmi, Double rightArmKg, Double leftArmKg, Double trunkKg,
                        Double rightLegKg, Double leftLegKg) {
        this.userId = userId;
        this.measuredAt = measuredAt;
        this.source = source;
        this.weightKg = weightKg;
        this.skeletalMuscleKg = skeletalMuscleKg;
        this.bodyFatPercent = bodyFatPercent;
        this.bmi = bmi;
        this.rightArmKg = rightArmKg;
        this.leftArmKg = leftArmKg;
        this.trunkKg = trunkKg;
        this.rightLegKg = rightLegKg;
        this.leftLegKg = leftLegKg;
    }
}
