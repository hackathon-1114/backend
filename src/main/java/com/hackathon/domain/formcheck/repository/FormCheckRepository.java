package com.hackathon.domain.formcheck.repository;

import com.hackathon.domain.formcheck.entity.FormCheck;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormCheckRepository extends JpaRepository<FormCheck, Long> {

    List<FormCheck> findByUserIdOrderByPerformedAtDesc(Long userId);
}
