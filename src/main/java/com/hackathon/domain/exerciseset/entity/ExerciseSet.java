package com.hackathon.domain.exerciseset.entity;

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

/** 카메라로 추적한 운동 한 세트. 반복별 수치를 그대로 보관하고, 통계는 읽을 때 다시 계산한다. */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseSet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, length = 30)
    private String exercise;

    /** 세트를 한 시각 (한국 시간) */
    @Column(nullable = false)
    private LocalDateTime performedAt;

    @Column(length = 30)
    private String targetMuscle;

    /** 근전도 값이 센서가 아니라 시뮬레이션에서 나왔는지 */
    private boolean emgSimulated;

    /** 반복별 수치 (JSON 배열, 모양은 ExerciseSetRequest.Rep) */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String repsJson;

    /** 근육별 반복당 RMS (JSON 배열, 모양은 ExerciseSetRequest.Muscle). 없으면 null */
    @Column(columnDefinition = "TEXT")
    private String musclesJson;

    /** AI 피드백 (JSON, 모양은 ExerciseSetResponse.Feedback). 요청 전이면 null */
    @Column(columnDefinition = "TEXT")
    private String feedbackJson;

    @Builder
    public ExerciseSet(Long userId, String exercise, LocalDateTime performedAt, String targetMuscle,
                       boolean emgSimulated, String repsJson, String musclesJson) {
        this.userId = userId;
        this.exercise = exercise;
        this.performedAt = performedAt;
        this.targetMuscle = targetMuscle;
        this.emgSimulated = emgSimulated;
        this.repsJson = repsJson;
        this.musclesJson = musclesJson;
    }

    public void attachFeedback(String feedbackJson) {
        this.feedbackJson = feedbackJson;
    }
}
