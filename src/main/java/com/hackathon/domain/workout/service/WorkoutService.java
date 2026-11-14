package com.hackathon.domain.workout.service;

import com.hackathon.domain.workout.dto.WorkoutRequest;
import com.hackathon.domain.workout.dto.WorkoutResponse;
import com.hackathon.domain.workout.entity.Workout;
import com.hackathon.domain.workout.repository.WorkoutRepository;
import com.hackathon.global.error.BusinessException;
import com.hackathon.global.error.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutService {

    /** 배포 서버 시계는 UTC라서, 시간대 패턴이 어긋나지 않도록 한국 시간을 명시한다 */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final WorkoutRepository workoutRepository;

    @Transactional
    public WorkoutResponse create(Long userId, WorkoutRequest.Create req) {
        Workout saved = workoutRepository.save(Workout.builder()
                .userId(userId)
                .type(req.type())
                .performedAt(req.performedAt() != null ? req.performedAt()
                        : LocalDateTime.now(KST).truncatedTo(ChronoUnit.MINUTES))
                .durationMinutes(req.durationMinutes())
                .distanceKm(req.distanceKm())
                .intensity(req.intensity())
                .mood(req.mood())
                .memo(req.memo())
                .build());
        return WorkoutResponse.from(saved);
    }

    public List<WorkoutResponse> findAll(Long userId) {
        List<Workout> list = (userId == null)
                ? workoutRepository.findAllByOrderByPerformedAtDesc()
                : workoutRepository.findByUserIdOrderByPerformedAtDesc(userId);
        return list.stream().map(WorkoutResponse::from).toList();
    }

    public WorkoutResponse findOne(Long id) {
        return WorkoutResponse.from(get(id));
    }

    @Transactional
    public WorkoutResponse update(Long id, WorkoutRequest.Update req) {
        Workout workout = get(id);
        workout.update(req.type(), req.performedAt(), req.durationMinutes(), req.distanceKm(),
                req.intensity(), req.mood(), req.memo());
        return WorkoutResponse.from(workout);
    }

    @Transactional
    public void delete(Long id) {
        workoutRepository.delete(get(id));
    }

    private Workout get(Long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKOUT_NOT_FOUND));
    }
}
