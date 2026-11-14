package com.hackathon.domain.survey.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyResponse(
        Long id,
        String goal,
        String experience,
        List<SurveyRequest.Discomfort> discomforts,
        String memo,
        LocalDateTime createdAt) {
}
