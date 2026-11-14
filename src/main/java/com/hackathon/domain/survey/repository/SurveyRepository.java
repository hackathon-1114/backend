package com.hackathon.domain.survey.repository;

import com.hackathon.domain.survey.entity.Survey;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    Optional<Survey> findFirstByUserIdOrderByIdDesc(Long userId);
}
