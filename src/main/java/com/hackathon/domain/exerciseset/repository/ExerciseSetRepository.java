package com.hackathon.domain.exerciseset.repository;

import com.hackathon.domain.exerciseset.entity.ExerciseSet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseSetRepository extends JpaRepository<ExerciseSet, Long> {

    List<ExerciseSet> findByUserIdOrderByPerformedAtDesc(Long userId);
}
