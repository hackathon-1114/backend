package com.hackathon.domain.formcheck.entity;

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

/** 운동 영상에서 뽑은 장면을 AI가 점검한 결과 한 건. 쌓이면 패턴 분석(InsightService)의 입력이 된다. */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormCheck extends BaseTimeEntity {

    public static final int LABELS_MAX = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    /** 운동 종목. 예: 스쿼트, 러닝 */
    @Column(nullable = false, length = 30)
    private String exercise;

    /** 영상을 찍은 시각 (한국 시간) */
    @Column(nullable = false)
    private LocalDateTime performedAt;

    /** 자세 점수 0~100 */
    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String summary;

    /** 잘한 점 목록 (JSON 배열 문자열) */
    @Column(columnDefinition = "TEXT")
    private String goodPointsJson;

    /** 지적 사항 목록 (JSON 배열 문자열). 모양은 FormCheckResponse.Issue */
    @Column(columnDefinition = "TEXT")
    private String issuesJson;

    /** 지적 사항 이름만 쉼표로 이은 것. 패턴 분석 프롬프트에 쓴다 */
    @Column(length = LABELS_MAX)
    private String issueLabels;

    @Column(length = 300)
    private String memo;

    @Builder
    public FormCheck(Long userId, String exercise, LocalDateTime performedAt, Integer score, String summary,
                     String goodPointsJson, String issuesJson, String issueLabels, String memo) {
        this.userId = userId;
        this.exercise = exercise;
        this.performedAt = performedAt;
        this.score = score;
        this.summary = summary;
        this.goodPointsJson = goodPointsJson;
        this.issuesJson = issuesJson;
        this.issueLabels = issueLabels;
        this.memo = memo;
    }
}
