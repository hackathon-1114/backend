package com.hackathon.domain.inbody.repository;

import com.hackathon.domain.inbody.entity.InbodyRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InbodyRepository extends JpaRepository<InbodyRecord, Long> {

    List<InbodyRecord> findByUserIdOrderByMeasuredAtDescIdDesc(Long userId);
}
