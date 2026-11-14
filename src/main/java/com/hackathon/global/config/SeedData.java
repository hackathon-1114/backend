package com.hackathon.global.config;

import com.hackathon.domain.sample.entity.Sample;
import com.hackathon.domain.sample.repository.SampleRepository;
import com.hackathon.domain.workout.entity.Workout;
import com.hackathon.domain.workout.repository.WorkoutRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
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
    private final WorkoutRepository workoutRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (sampleRepository.count() == 0) {
            sampleRepository.saveAll(List.of(
                    Sample.builder().title("아침 혈압 기록").content("128/84, 두통 약간").userId(1L).build(),
                    Sample.builder().title("복약 체크").content("고혈압약 08:00 복용 완료").userId(1L).build(),
                    Sample.builder().title("수면 기록").content("6시간 30분, 중간에 2번 깸").userId(2L).build()));
        }
        if (workoutRepository.count() == 0) {
            workoutRepository.saveAll(demoWorkouts());
        }
    }

    /**
     * 유저 1의 최근 5주치 운동 기록. 데모에서 AI가 찾아낼 패턴을 일부러 심어 두었다.
     * - 11km 넘게 뛴 일요일 뒤에는 4~5일을 쉰다 (8~9km 뒤에는 이틀 만에 다시 뛴다)
     * - 아침 운동은 기분 5, 평일 저녁 운동은 기분 2~3
     * - 야근한 날 억지로 간 헬스 뒤에는 나흘 공백이 생긴다
     * 요일 패턴이 재시작해도 유지되도록, 날짜는 가장 최근 일요일을 기준으로 계산한다.
     */
    private List<Workout> demoWorkouts() {
        LocalDate lastSunday = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        return List.of(
                // 5주 전
                running(lastSunday,4, DayOfWeek.TUESDAY, 19, 30, 35, 5.0, 3, 3, "퇴근 후 한강"),
                running(lastSunday,4, DayOfWeek.THURSDAY, 19, 40, 30, 4.5, 3, 3, null),
                running(lastSunday,4, DayOfWeek.SATURDAY, 7, 10, 40, 6.0, 3, 5, "아침 공기가 좋았음"),
                running(lastSunday,4, DayOfWeek.SUNDAY, 8, 0, 75, 12.0, 5, 4, "처음으로 12km"),
                // 4주 전: 장거리 뒤 5일 공백
                running(lastSunday,3, DayOfWeek.FRIDAY, 20, 10, 25, 3.5, 2, 2, "다리가 무거움"),
                running(lastSunday,3, DayOfWeek.SUNDAY, 7, 30, 50, 8.0, 4, 5, null),
                // 3주 전: 야근한 수요일 뒤 4일 공백
                running(lastSunday,2, DayOfWeek.TUESDAY, 19, 20, 35, 5.2, 3, 3, null),
                gym(lastSunday, 2, DayOfWeek.WEDNESDAY, 21, 40, 30, 2, 2, "야근하고 늦게 감, 집중이 안 됨"),
                running(lastSunday,2, DayOfWeek.SUNDAY, 8, 10, 70, 11.0, 5, 4, "장거리"),
                // 2주 전: 장거리 뒤 4일 공백
                running(lastSunday,1, DayOfWeek.THURSDAY, 19, 50, 30, 4.0, 3, 2, "오랜만이라 힘듦"),
                running(lastSunday,1, DayOfWeek.SATURDAY, 7, 0, 45, 7.0, 3, 5, "상쾌함"),
                running(lastSunday,1, DayOfWeek.SUNDAY, 7, 40, 55, 9.0, 4, 5, null),
                // 지난주: 야근한 수요일 뒤 4일 공백
                running(lastSunday,0, DayOfWeek.TUESDAY, 19, 30, 35, 5.5, 3, 3, null),
                gym(lastSunday, 0, DayOfWeek.WEDNESDAY, 22, 0, 25, 2, 2, "야근 후 억지로 감"),
                running(lastSunday,0, DayOfWeek.SUNDAY, 8, 0, 80, 13.0, 5, 4, "최장 거리"));
    }

    private static Workout running(LocalDate lastSunday, int weeksAgo, DayOfWeek day, int hour, int minute,
                                   int minutes, double km, int intensity, int mood, String memo) {
        return workout("러닝", lastSunday, weeksAgo, day, hour, minute, minutes, km, intensity, mood, memo);
    }

    private static Workout gym(LocalDate lastSunday, int weeksAgo, DayOfWeek day, int hour, int minute,
                               int minutes, int intensity, int mood, String memo) {
        return workout("헬스", lastSunday, weeksAgo, day, hour, minute, minutes, null, intensity, mood, memo);
    }

    private static Workout workout(String type, LocalDate lastSunday, int weeksAgo, DayOfWeek day, int hour,
                                   int minute, int minutes, Double km, int intensity, int mood, String memo) {
        // lastSunday가 그 주의 마지막 날이 되도록 월요일~일요일을 한 주로 센다
        LocalDate date = lastSunday.minusWeeks(weeksAgo).minusDays(DayOfWeek.SUNDAY.getValue() - day.getValue());
        return Workout.builder()
                .userId(1L)
                .type(type)
                .performedAt(date.atTime(hour, minute))
                .durationMinutes(minutes)
                .distanceKm(km)
                .intensity(intensity)
                .mood(mood)
                .memo(memo)
                .build();
    }
}
