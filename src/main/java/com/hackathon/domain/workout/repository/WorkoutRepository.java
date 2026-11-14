package com.hackathon.domain.workout.repository;

import com.hackathon.domain.workout.entity.Workout;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    List<Workout> findAllByOrderByPerformedAtDesc();

    List<Workout> findByUserIdOrderByPerformedAtDesc(Long userId);
}
