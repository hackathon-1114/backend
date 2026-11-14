package com.hackathon.domain.bodyalert.repository;

import com.hackathon.domain.bodyalert.entity.BodyAlert;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyAlertRepository extends JpaRepository<BodyAlert, Long> {

    Optional<BodyAlert> findFirstByUserIdOrderByIdDesc(Long userId);
}
