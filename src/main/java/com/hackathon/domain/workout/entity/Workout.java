package com.hackathon.domain.workout.entity;

import com.hackathon.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 운동 한 번의 기록. 패턴 분석(InsightService)의 입력이 된다. */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workout extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인 없이 X-User-Id 헤더로 받은 값 */
    private Long userId;

    /** 운동 종류. 예: 러닝, 헬스, 수영 */
    @Column(nullable = false, length = 30)
    private String type;

    /** 운동을 시작한 시각 (한국 시간) */
    @Column(nullable = false)
    private LocalDateTime performedAt;

    @Column(nullable = false)
    private Integer durationMinutes;

    private Double distanceKm;

    /** 체감 강도 1(가벼움)~5(한계) */
    private Integer intensity;

    /** 운동 후 기분 1(나쁨)~5(좋음) */
    private Integer mood;

    @Column(length = 500)
    private String memo;

    @Builder
    public Workout(Long userId, String type, LocalDateTime performedAt, Integer durationMinutes,
                   Double distanceKm, Integer intensity, Integer mood, String memo) {
        this.userId = userId;
        this.type = type;
        this.performedAt = performedAt;
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
        this.intensity = intensity;
        this.mood = mood;
        this.memo = memo;
    }

    public void update(String type, LocalDateTime performedAt, Integer durationMinutes,
                       Double distanceKm, Integer intensity, Integer mood, String memo) {
        if (type != null) this.type = type;
        if (performedAt != null) this.performedAt = performedAt;
        if (durationMinutes != null) this.durationMinutes = durationMinutes;
        if (distanceKm != null) this.distanceKm = distanceKm;
        if (intensity != null) this.intensity = intensity;
        if (mood != null) this.mood = mood;
        if (memo != null) this.memo = memo;
    }
}
