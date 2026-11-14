package com.hackathon.domain.insight.entity;

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

/** AI가 운동 기록에서 찾아낸 패턴 하나. */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Insight extends BaseTimeEntity {

    public static final int TITLE_MAX = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    /** 패턴 한 줄 요약 */
    @Column(nullable = false, length = TITLE_MAX)
    private String title;

    /** 그렇게 본 근거 (기록의 날짜와 수치) */
    @Column(columnDefinition = "TEXT")
    private String evidence;

    /** 이번 주에 해볼 행동 */
    @Column(columnDefinition = "TEXT")
    private String suggestion;

    @Builder
    public Insight(Long userId, String title, String evidence, String suggestion) {
        this.userId = userId;
        this.title = title;
        this.evidence = evidence;
        this.suggestion = suggestion;
    }
}
