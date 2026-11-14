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

    @Builder
    public Survey(Long userId, String goal, String experience, String discomfortsJson, String memo) {
        this.userId = userId;
        this.goal = goal;
        this.experience = experience;
        this.discomfortsJson = discomfortsJson;
        this.memo = memo;
    }
}
