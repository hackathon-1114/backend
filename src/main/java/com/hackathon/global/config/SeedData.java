package com.hackathon.global.config;

import com.hackathon.domain.exerciseset.dto.ExerciseSetRequest;
import com.hackathon.domain.exerciseset.entity.ExerciseSet;
import com.hackathon.domain.exerciseset.repository.ExerciseSetRepository;
import com.hackathon.domain.formcheck.entity.FormCheck;
import com.hackathon.domain.formcheck.repository.FormCheckRepository;
import com.hackathon.domain.inbody.entity.InbodyRecord;
import com.hackathon.domain.inbody.repository.InbodyRepository;
import com.hackathon.domain.sample.entity.Sample;
import com.hackathon.domain.sample.repository.SampleRepository;
import com.hackathon.domain.survey.entity.Survey;
import com.hackathon.domain.survey.repository.SurveyRepository;
import com.hackathon.domain.workout.entity.Workout;
import com.hackathon.domain.workout.repository.WorkoutRepository;
import com.hackathon.global.common.Json;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
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
    private final FormCheckRepository formCheckRepository;
    private final ExerciseSetRepository exerciseSetRepository;
    private final InbodyRepository inbodyRepository;
    private final SurveyRepository surveyRepository;
    private final Json json;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (sampleRepository.count() == 0) {
            sampleRepository.saveAll(List.of(
                    Sample.builder().title("아침 혈압 기록").content("128/84, 두통 약간").userId(1L).build(),
                    Sample.builder().title("복약 체크").content("고혈압약 08:00 복용 완료").userId(1L).build(),
                    Sample.builder().title("수면 기록").content("6시간 30분, 중간에 2번 깸").userId(2L).build()));
        }
        LocalDate lastSunday = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        if (workoutRepository.count() == 0) {
            workoutRepository.saveAll(demoWorkouts(lastSunday));
        }
        if (formCheckRepository.count() == 0) {
            formCheckRepository.saveAll(demoFormChecks(lastSunday));
        }
        if (exerciseSetRepository.count() == 0) {
            exerciseSetRepository.saveAll(demoSets(lastSunday));
        }
        if (inbodyRepository.count() == 0) {
            // 왼다리 근육량이 오른다리보다 7.5% 적다. 문진의 왼쪽 무릎 불편함과 같은 쪽으로 맞췄다
            inbodyRepository.save(InbodyRecord.builder().userId(1L).source("manual").measuredAt(lastSunday.minusDays(3))
                    .weightKg(68.4).skeletalMuscleKg(30.1).bodyFatPercent(18.5)
                    .rightArmKg(3.0).leftArmKg(2.9).trunkKg(24.2).rightLegKg(9.3).leftLegKg(8.6).build());
        }
        if (surveyRepository.count() == 0) {
            surveyRepository.save(Survey.builder().userId(1L).goal("하체 근력 강화").experience("헬스 6개월")
                    .discomfortsJson("[{\"bodyPart\":\"왼쪽 무릎\",\"situation\":\"계단을 내려갈 때\",\"level\":3}]")
                    .build());
        }
    }

    /**
     * 유저 1의 최근 5주치 운동 기록. 데모에서 AI가 찾아낼 패턴을 일부러 심어 두었다.
     * - 11km 넘게 뛴 일요일 뒤에는 4~5일을 쉰다 (8~9km 뒤에는 이틀 만에 다시 뛴다)
     * - 아침 운동은 기분 5, 평일 저녁 운동은 기분 2~3
     * - 야근한 날 억지로 간 헬스 뒤에는 나흘 공백이 생긴다
     * 요일 패턴이 재시작해도 유지되도록, 날짜는 가장 최근 일요일을 기준으로 계산한다.
     */
    private List<Workout> demoWorkouts(LocalDate lastSunday) {
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
        return Workout.builder()
                .userId(1L)
                .type(type)
                .performedAt(dateOf(lastSunday, weeksAgo, day).atTime(hour, minute))
                .durationMinutes(minutes)
                .distanceKm(km)
                .intensity(intensity)
                .mood(mood)
                .memo(memo)
                .build();
    }

    /** lastSunday가 그 주의 마지막 날이 되도록 월요일~일요일을 한 주로 센다 */
    private static LocalDate dateOf(LocalDate lastSunday, int weeksAgo, DayOfWeek day) {
        return lastSunday.minusWeeks(weeksAgo).minusDays(DayOfWeek.SUNDAY.getValue() - day.getValue());
    }

    /**
     * 유저 1의 자세 점검 기록. 위 운동 기록과 같은 날짜에 맞췄다.
     * 심어 둔 패턴: 야근한 날 늦은 저녁 세션은 점수가 낮고 같은 지적이 반복되고, 주말 아침 세션은 지적이 없다.
     */
    private List<FormCheck> demoFormChecks(LocalDate lastSunday) {
        String kneeIn = """
                {"label":"무릎 안쪽 모임","bodyPart":"무릎","observation":"일어서는 장면에서 양 무릎이 발끝보다 안쪽으로 들어옵니다",\
                "cue":"무릎을 새끼발가락 쪽으로 밀면서 일어나세요","severity":"보통"}""";
        String shallow = """
                {"label":"깊이 부족","bodyPart":"고관절","observation":"가장 낮은 장면에서도 엉덩이가 무릎보다 높습니다",\
                "cue":"의자에 앉듯 엉덩이를 뒤로 빼며 조금 더 내려가세요","severity":"낮음"}""";
        String lean = """
                {"label":"상체 숙임","bodyPart":"상체","observation":"후반 장면으로 갈수록 상체가 앞으로 기웁니다",\
                "cue":"시선을 15m 앞에 두고 가슴을 펴세요","severity":"낮음"}""";
        return List.of(
                formCheck(lastSunday, 4, DayOfWeek.SATURDAY, 7, 10, "러닝", 84,
                        "착지와 상체 정렬이 안정적입니다.", "[\"상체가 곧게 서 있습니다\",\"팔 흔들기가 일정합니다\"]", "[]", ""),
                formCheck(lastSunday, 3, DayOfWeek.FRIDAY, 20, 10, "러닝", 66,
                        "후반으로 갈수록 상체가 앞으로 기웁니다.", "[\"보폭이 일정합니다\"]", "[" + lean + "]", "상체 숙임"),
                formCheck(lastSunday, 2, DayOfWeek.WEDNESDAY, 21, 40, "스쿼트", 61,
                        "일어설 때 무릎이 안으로 모이고 깊이가 얕습니다.", "[\"발 간격이 적절합니다\"]",
                        "[" + kneeIn + "," + shallow + "]", "무릎 안쪽 모임, 깊이 부족"),
                formCheck(lastSunday, 1, DayOfWeek.SATURDAY, 7, 0, "러닝", 86,
                        "전반적으로 안정적인 자세입니다.", "[\"착지가 몸 아래에서 이뤄집니다\"]", "[]", ""),
                formCheck(lastSunday, 0, DayOfWeek.WEDNESDAY, 22, 0, "스쿼트", 58,
                        "지난번과 같은 무릎 모임이 다시 보입니다.", "[\"허리는 중립을 유지합니다\"]",
                        "[" + kneeIn + "," + shallow + "]", "무릎 안쪽 모임, 깊이 부족"));
    }

    private static FormCheck formCheck(LocalDate lastSunday, int weeksAgo, DayOfWeek day, int hour, int minute,
                                       String exercise, int score, String summary, String goodPointsJson,
                                       String issuesJson, String issueLabels) {
        return FormCheck.builder()
                .userId(1L)
                .exercise(exercise)
                .performedAt(dateOf(lastSunday, weeksAgo, day).atTime(hour, minute))
                .score(score)
                .summary(summary)
                .goodPointsJson(goodPointsJson)
                .issuesJson(issuesJson)
                .issueLabels(issueLabels)
                .build();
    }

    /**
     * 유저 1의 카메라 세트 기록 (측면 스쿼트 10회씩). 심어 둔 패턴:
     * 야근한 수요일 밤 세트는 5~6회째부터 깊이가 얕아지고 느려지며 상체가 숙여지고, 토요일 아침 세트는 끝까지 일정하다.
     */
    private List<ExerciseSet> demoSets(LocalDate lastSunday) {
        return List.of(
                set(lastSunday, 2, DayOfWeek.WEDNESDAY, 21, 45, 6),
                set(lastSunday, 1, DayOfWeek.SATURDAY, 7, 20, 0),
                set(lastSunday, 0, DayOfWeek.WEDNESDAY, 22, 5, 5));
    }

    /** fadeFrom회째부터 무너지는 10회 세트를 만든다. 0이면 끝까지 일정하다 */
    private ExerciseSet set(LocalDate lastSunday, int weeksAgo, DayOfWeek day, int hour, int minute, int fadeFrom) {
        List<ExerciseSetRequest.Rep> reps = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            int tired = fadeFrom > 0 && i >= fadeFrom ? i - fadeFrom + 1 : 0;
            double depth = 0.50 + ((i * 7) % 3 - 1) * 0.005 + (tired > 0 ? 0.04 + 0.02 * tired : 0);
            double thigh = Math.round((depth - 0.47) * 1500) / 10.0;
            reps.add(new ExerciseSetRequest.Rep("side", Math.round(depth * 1000) / 1000.0, 2200 + 230 * tired,
                    32.0 + 2.5 * tired, thigh, null, null, thigh > 15 ? List.of("깊이 부족") : List.of()));
        }
        return ExerciseSet.builder()
                .userId(1L)
                .exercise("스쿼트")
                .performedAt(dateOf(lastSunday, weeksAgo, day).atTime(hour, minute))
                .repsJson(json.write(reps))
                .build();
    }
}
