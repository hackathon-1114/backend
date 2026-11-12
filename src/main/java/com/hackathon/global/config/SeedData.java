package com.hackathon.global.config;

import com.hackathon.domain.sample.entity.Sample;
import com.hackathon.domain.sample.repository.SampleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** DB가 비어 있으면 데모용 데이터를 넣는다. 현장에서 주제에 맞게 내용만 바꾸기. */
@Component
@RequiredArgsConstructor
public class SeedData implements ApplicationRunner {

    private final SampleRepository sampleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (sampleRepository.count() > 0) return;
        sampleRepository.saveAll(List.of(
                Sample.builder().title("아침 혈압 기록").content("128/84, 두통 약간").userId(1L).build(),
                Sample.builder().title("복약 체크").content("고혈압약 08:00 복용 완료").userId(1L).build(),
                Sample.builder().title("수면 기록").content("6시간 30분, 중간에 2번 깸").userId(2L).build()));
    }
}
