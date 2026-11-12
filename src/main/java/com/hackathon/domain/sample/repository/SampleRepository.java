package com.hackathon.domain.sample.repository;

import com.hackathon.domain.sample.entity.Sample;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleRepository extends JpaRepository<Sample, Long> {

    List<Sample> findAllByOrderByIdDesc();

    List<Sample> findByUserIdOrderByIdDesc(Long userId);
}
