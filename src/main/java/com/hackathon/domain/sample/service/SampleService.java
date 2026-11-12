package com.hackathon.domain.sample.service;

import com.hackathon.domain.sample.dto.SampleRequest;
import com.hackathon.domain.sample.dto.SampleResponse;
import com.hackathon.domain.sample.entity.Sample;
import com.hackathon.domain.sample.repository.SampleRepository;
import com.hackathon.global.error.BusinessException;
import com.hackathon.global.error.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SampleService {

    private final SampleRepository sampleRepository;

    @Transactional
    public SampleResponse create(Long userId, SampleRequest.Create req) {
        Sample saved = sampleRepository.save(Sample.builder()
                .title(req.title())
                .content(req.content())
                .userId(userId)
                .build());
        return SampleResponse.from(saved);
    }

    public List<SampleResponse> findAll(Long userId) {
        List<Sample> list = (userId == null)
                ? sampleRepository.findAllByOrderByIdDesc()
                : sampleRepository.findByUserIdOrderByIdDesc(userId);
        return list.stream().map(SampleResponse::from).toList();
    }

    public SampleResponse findOne(Long id) {
        return SampleResponse.from(get(id));
    }

    @Transactional
    public SampleResponse update(Long id, SampleRequest.Update req) {
        Sample sample = get(id);
        sample.update(req.title(), req.content());
        return SampleResponse.from(sample);
    }

    @Transactional
    public void delete(Long id) {
        sampleRepository.delete(get(id));
    }

    private Sample get(Long id) {
        return sampleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SAMPLE_NOT_FOUND));
    }
}
