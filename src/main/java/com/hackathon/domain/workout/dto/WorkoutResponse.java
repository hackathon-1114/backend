package com.hackathon.domain.workout.dto;

import com.hackathon.domain.workout.entity.Workout;
import java.time.LocalDateTime;

public record WorkoutResponse(
        Long id,
        Long userId,
        String type,
        LocalDateTime performedAt,
        Integer durationMinutes,
        Double distanceKm,
        Integer intensity,
        Integer mood,
        String memo,
        LocalDateTime createdAt) {

    public static WorkoutResponse from(Workout w) {
        return new WorkoutResponse(w.getId(), w.getUserId(), w.getType(), w.getPerformedAt(),
                w.getDurationMinutes(), w.getDistanceKm(), w.getIntensity(), w.getMood(), w.getMemo(),
                w.getCreatedAt());
    }
}
