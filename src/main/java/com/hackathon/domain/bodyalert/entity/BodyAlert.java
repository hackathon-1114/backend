package com.hackathon.domain.bodyalert.entity;

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

/** 카메라로 재기 전에 인바디와 문진만으로 만든 사전 분석 한 건 (STEP 03 BODY ALERT). */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BodyAlert extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String summary;

    /** 관찰 영역 목록 (JSON 배열, 모양은 BodyAlertResponse.Area) */
    @Column(columnDefinition = "TEXT")
    private String areasJson;

    @Column(columnDefinition = "TEXT")
    private String nextStep;

    @Builder
    public BodyAlert(Long userId, String summary, String areasJson, String nextStep) {
        this.userId = userId;
        this.summary = summary;
        this.areasJson = areasJson;
        this.nextStep = nextStep;
    }
}
