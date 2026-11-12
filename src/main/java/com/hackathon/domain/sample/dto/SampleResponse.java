package com.hackathon.domain.sample.dto;

import com.hackathon.domain.sample.entity.Sample;
import java.time.LocalDateTime;

public record SampleResponse(
        Long id,
        String title,
        String content,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static SampleResponse from(Sample s) {
        return new SampleResponse(s.getId(), s.getTitle(), s.getContent(), s.getUserId(),
                s.getCreatedAt(), s.getUpdatedAt());
    }
}
