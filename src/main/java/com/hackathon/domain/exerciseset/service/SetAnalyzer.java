package com.hackathon.domain.exerciseset.service;

import com.hackathon.domain.exerciseset.dto.ExerciseSetRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * 반복별 수치에서 세트 통계를 계산한다. AI를 쓰지 않는 순수 계산이라 같은 입력이면 항상 같은 결과가 나온다.
 *
 * 절대 각도 대신 "세트 앞부분 대비 뒷부분의 변화"를 본다. 관절 추적의 절대 각도는 오차가 크지만
 * 같은 사람, 같은 카메라 위치에서의 반복 간 변화는 안정적으로 잡힌다 (docs/verification.md).
 */
public final class SetAnalyzer {

    /** 깊이가 세트 초반보다 이만큼(엉덩이 높이 비율) 얕아지면 흐트러진 반복으로 본다 */
    static final double FADE_DEPTH = 0.05;
    /** 걸린 시간이 세트 초반의 이 배수를 넘으면 흐트러진 반복으로 본다 */
    static final double FADE_TEMPO = 1.30;
    /** 흐트러짐 판단을 하려면 최소 이만큼은 반복해야 한다 */
    static final int MIN_REPS_FOR_FADE = 4;

    private SetAnalyzer() {
    }

    public record Stats(
            int repCount,
            @Schema(description = "평균 깊이. 엉덩이 높이 비율이라 낮을수록 깊다", example = "0.55")
            double avgDepth,
            @Schema(description = "가장 깊었던 반복의 깊이", example = "0.48")
            double bestDepth,
            @Schema(description = "세트 뒤 1/3의 평균 깊이에서 앞 1/3을 뺀 값. 양수면 뒤로 갈수록 얕아진 것", example = "0.07")
            double depthChange,
            @Schema(description = "뒤 1/3이 앞 1/3보다 몇 % 느려졌나. 양수면 느려진 것", example = "32.0")
            double tempoChangePct,
            @Schema(description = "뒤 1/3의 상체 기울기에서 앞 1/3을 뺀 값(도). 측면 촬영에서만 의미 있음", example = "6.5")
            double leanChange,
            @Schema(description = "이 회차부터 깊이나 속도가 무너졌다. 1부터 센다. 무너지지 않았으면 null", example = "7")
            Integer fadeFromRep,
            @Schema(description = "뒤 1/3의 무릎 간격 비율이 앞 1/3보다 몇 % 달라졌나. 음수면 뒤로 갈수록 무릎이 모인 것. "
                    + "정면 촬영 반복이 3회 미만이면 null", example = "-14.0")
            Double kneeRatioChangePct,
            @Schema(description = "관찰별 횟수", example = "{\"깊이 부족\": 3}")
            Map<String, Integer> findingCounts) {
    }

    public record Emg(
            @Schema(description = "센서가 아니라 시뮬레이션 값이면 true")
            boolean simulated,
            @Schema(description = "전체 근활성 중 목표 근육이 차지한 비율(%). 목표 근육을 안 보냈으면 null", example = "34.0")
            Double targetSharePct,
            List<Asymmetry> asymmetries) {
    }

    public record Asymmetry(
            String muscle,
            double leftMean,
            double rightMean,
            @Schema(description = "좌우 차이(%). 큰 쪽 기준", example = "18.0")
            double diffPct,
            @Schema(description = "더 많이 쓴 쪽. left / right", example = "right")
            String stronger) {
    }

    public static Stats analyze(List<ExerciseSetRequest.Rep> reps) {
        int n = reps.size();
        int third = Math.max(1, n / 3);
        List<ExerciseSetRequest.Rep> first = reps.subList(0, third);
        List<ExerciseSetRequest.Rep> last = reps.subList(n - third, n);

        double firstDuration = mean(first, r -> r.durationMs());
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ExerciseSetRequest.Rep r : reps) {
            if (r.findings() != null) r.findings().forEach(f -> counts.merge(f, 1, Integer::sum));
        }
        return new Stats(
                n,
                round(mean(reps, ExerciseSetRequest.Rep::depth), 3),
                round(reps.stream().mapToDouble(ExerciseSetRequest.Rep::depth).min().orElse(0), 3),
                round(mean(last, ExerciseSetRequest.Rep::depth) - mean(first, ExerciseSetRequest.Rep::depth), 3),
                round(firstDuration == 0 ? 0 : (mean(last, r -> r.durationMs()) / firstDuration - 1) * 100, 1),
                round(mean(last, SetAnalyzer::lean) - mean(first, SetAnalyzer::lean), 1),
                fadeFromRep(reps),
                kneeRatioChangePct(reps),
                counts);
    }

    /**
     * 처음 두 번을 기준으로, 깊이가 얕아지거나 느려진 상태가 "그 뒤로 절반 넘게" 이어지기 시작한 회차를 찾는다.
     * 한 번 삐끗한 것은 흐트러짐으로 보지 않는다.
     */
    static Integer fadeFromRep(List<ExerciseSetRequest.Rep> reps) {
        int n = reps.size();
        if (n < MIN_REPS_FOR_FADE) return null;
        double baseDepth = mean(reps.subList(0, 2), ExerciseSetRequest.Rep::depth);
        double baseDuration = mean(reps.subList(0, 2), r -> r.durationMs());
        boolean[] faded = new boolean[n];
        for (int i = 2; i < n; i++) {
            ExerciseSetRequest.Rep r = reps.get(i);
            faded[i] = r.depth() - baseDepth >= FADE_DEPTH || r.durationMs() >= baseDuration * FADE_TEMPO;
        }
        for (int i = 2; i < n; i++) {
            if (!faded[i]) continue;
            int rest = n - i, fadedRest = 0;
            for (int j = i; j < n; j++) if (faded[j]) fadedRest++;
            if (fadedRest * 2 > rest) return i + 1;
        }
        return null;
    }

    /**
     * 무릎 간격의 절대 기준(발목 간격의 80% 미만)은 검증하지 못했다. 대신 같은 사람의 세트 안에서
     * 뒤로 갈수록 무릎 간격이 좁아지는지를 본다. 반복 간 변화는 절대값보다 안정적으로 잡힌다.
     */
    static Double kneeRatioChangePct(List<ExerciseSetRequest.Rep> reps) {
        List<Double> ratios = reps.stream().map(ExerciseSetRequest.Rep::kneeRatio).filter(v -> v != null && v > 0).toList();
        if (ratios.size() < 3) return null;
        int third = Math.max(1, ratios.size() / 3);
        double first = ratios.subList(0, third).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double last = ratios.subList(ratios.size() - third, ratios.size()).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return first == 0 ? null : round((last / first - 1) * 100, 1);
    }

    public static Emg analyzeEmg(List<ExerciseSetRequest.Muscle> muscles, String targetMuscle, boolean simulated) {
        if (muscles == null || muscles.isEmpty()) return null;
        Map<String, Double> left = new LinkedHashMap<>(), right = new LinkedHashMap<>();
        double total = 0, target = 0;
        for (ExerciseSetRequest.Muscle m : muscles) {
            double avg = m.rmsPerRep().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            total += avg;
            if (m.name().equals(targetMuscle)) target += avg;
            if ("left".equals(m.side())) left.put(m.name(), avg);
            if ("right".equals(m.side())) right.put(m.name(), avg);
        }
        List<Asymmetry> asymmetries = new ArrayList<>();
        left.forEach((name, l) -> {
            Double r = right.get(name);
            if (r == null) return;
            double max = Math.max(l, r);
            asymmetries.add(new Asymmetry(name, round(l, 3), round(r, 3),
                    max == 0 ? 0 : round(Math.abs(l - r) / max * 100, 1), r >= l ? "right" : "left"));
        });
        Double share = targetMuscle == null || total == 0 ? null : round(target / total * 100, 1);
        return new Emg(simulated, share, asymmetries);
    }

    private static double lean(ExerciseSetRequest.Rep r) {
        return r.maxLean() == null ? 0 : r.maxLean();
    }

    private static double mean(List<ExerciseSetRequest.Rep> reps, ToDoubleFunction<ExerciseSetRequest.Rep> f) {
        return reps.stream().mapToDouble(f).average().orElse(0);
    }

    private static double round(double v, int digits) {
        double p = Math.pow(10, digits);
        return Math.round(v * p) / p;
    }
}
