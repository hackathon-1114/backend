package com.hackathon.domain.insight.repository;

import com.hackathon.domain.insight.entity.Insight;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsightRepository extends JpaRepository<Insight, Long> {

    List<Insight> findByUserIdOrderByIdAsc(Long userId);
}
