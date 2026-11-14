package com.hackathon.domain.survey.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hackathon.domain.survey.dto.SurveyRequest;
import com.hackathon.domain.survey.dto.SurveyResponse;
import com.hackathon.domain.survey.entity.Survey;
import com.hackathon.domain.survey.repository.SurveyRepository;
import com.hackathon.global.common.Json;
import com.hackathon.global.error.BusinessException;
import com.hackathon.global.error.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private static final TypeReference<List<SurveyRequest.Discomfort>> DISCOMFORTS = new TypeReference<>() { };

    private final SurveyRepository surveyRepository;
    private final Json json;

    public SurveyResponse create(Long userId, SurveyRequest.Create req) {
        return toResponse(surveyRepository.save(Survey.builder()
                .userId(userId)
                .goal(req.goal())
                .experience(req.experience())
                .discomfortsJson(json.write(req.discomforts() == null ? List.of() : req.discomforts()))
                .memo(req.memo())
                .build()));
    }

    public SurveyResponse findLatest(Long userId) {
        return toResponse(surveyRepository.findFirstByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND)));
    }

    /** AI 프롬프트에 붙일 최신 문진 요약. 문진이 없으면 빈 문자열 */
    public String describeLatest(Long userId) {
        return surveyRepository.findFirstByUserIdOrderByIdDesc(userId).map(s -> {
            StringBuilder sb = new StringBuilder("\n사용자 문진 (본인이 직접 적은 내용)\n");
            if (notBlank(s.getGoal())) sb.append("목표: ").append(s.getGoal()).append('\n');
            if (notBlank(s.getExperience())) sb.append("경력: ").append(s.getExperience()).append('\n');
            List<SurveyRequest.Discomfort> list = json.read(s.getDiscomfortsJson(), DISCOMFORTS);
            if (list == null || list.isEmpty()) {
                sb.append("불편함: 없다고 답함\n");
            } else {
                for (SurveyRequest.Discomfort d : list) {
                    sb.append("불편함: ").append(d.bodyPart());
                    if (notBlank(d.situation())) sb.append(", ").append(d.situation());
                    if (d.level() != null) sb.append(", 정도 ").append(d.level()).append("/10");
                    sb.append('\n');
                }
            }
            if (notBlank(s.getMemo())) sb.append("메모: ").append(s.getMemo()).append('\n');
            return sb.toString();
        }).orElse("");
    }

    private SurveyResponse toResponse(Survey s) {
        List<SurveyRequest.Discomfort> list = json.read(s.getDiscomfortsJson(), DISCOMFORTS);
        return new SurveyResponse(s.getId(), s.getGoal(), s.getExperience(), list == null ? List.of() : list,
                s.getMemo(), s.getCreatedAt());
    }

    private static boolean notBlank(String v) {
        return v != null && !v.isBlank();
    }
}
