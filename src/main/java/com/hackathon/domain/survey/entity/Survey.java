package com.hackathon.domain.survey.entity;

import com.hackathon.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 문진 한 번. 다시 작성하면 새 행이 쌓이고 가장 최근 것을 쓴다. */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Survey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    /** 운동 목표. 예: 하체 근력 */
    @Column(length = 100)
    private String goal;

    /** 운동 경력. 예: 6개월 */
    @Column(length = 30)
    private String experience;

    /** 불편함 목록 (JSON 배열, 모양은 SurveyRequest.Discomfort) */
    @Column(columnDefinition = "TEXT")
    private String discomfortsJson;

    @Column(length = 500)
    private String memo;

    /** 하루 평균 앉아 있는 시간. 예: 4~8시간 */
    @Column(length = 20)
    private String sittingHours;

    /** 주당 운동 횟수 */
    private Integer weeklyWorkouts;

    /** 신체 사용 습관 (JSON 문자열 배열). 예: ["다리를 자주 꼰다"] */
    @Column(columnDefinition = "TEXT")
    private String bodyHabitsJson;

    @Builder
    public Survey(Long userId, String goal, String experience, String discomfortsJson, String memo,
                  String sittingHours, Integer weeklyWorkouts, String bodyHabitsJson) {
        this.sittingHours = sittingHours;
        this.weeklyWorkouts = weeklyWorkouts;
        this.bodyHabitsJson = bodyHabitsJson;
        this.userId = userId;
        this.goal = goal;
        this.experience = experience;
        this.discomfortsJson = discomfortsJson;
        this.memo = memo;
    }
}
