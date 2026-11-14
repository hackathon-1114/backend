package com.hackathon.domain.exerciseset.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.hackathon.domain.exerciseset.dto.ExerciseSetRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SetAnalyzerTest {

    @Test
    void findsTheRepWhereDepthAndTempoStartToFade() {
        List<ExerciseSetRequest.Rep> reps = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            boolean tired = i >= 7;
            reps.add(rep(tired ? 0.60 : 0.50, tired ? 3000 : 2200, tired ? 40.0 : 32.0, tired ? List.of("깊이 부족") : List.of()));
        }

        SetAnalyzer.Stats stats = SetAnalyzer.analyze(reps);

        assertThat(stats.repCount()).isEqualTo(10);
        assertThat(stats.fadeFromRep()).isEqualTo(7);
        assertThat(stats.depthChange()).isEqualTo(0.1);
        assertThat(stats.tempoChangePct()).isEqualTo(36.4);
        assertThat(stats.leanChange()).isEqualTo(8.0);
        assertThat(stats.findingCounts()).containsEntry("깊이 부족", 4);
    }

    @Test
    void oneBadRepInTheMiddleIsNotAFade() {
        List<ExerciseSetRequest.Rep> reps = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            reps.add(rep(i == 4 ? 0.62 : 0.50, 2200, 30.0, List.of()));
        }

        assertThat(SetAnalyzer.analyze(reps).fadeFromRep()).isNull();
    }

    @Test
    void kneeSpacingChangeNeedsFrontViewReps() {
        List<ExerciseSetRequest.Rep> front = new ArrayList<>();
        for (double ratio : new double[] {1.30, 1.30, 1.28, 1.20, 1.10, 1.04}) {
            front.add(new ExerciseSetRequest.Rep("front", 0.6, 1500, 3.0, 60.0, ratio, 0.02, List.of()));
        }

        assertThat(SetAnalyzer.analyze(front).kneeRatioChangePct()).isEqualTo(-17.7);
        assertThat(SetAnalyzer.analyze(List.of(rep(0.5, 2000, 30.0, List.of()), rep(0.5, 2000, 30.0, List.of()),
                rep(0.5, 2000, 30.0, List.of()))).kneeRatioChangePct()).isNull();
    }

    @Test
    void shortSetsNeverReportAFade() {
        assertThat(SetAnalyzer.analyze(List.of(rep(0.5, 2000, 30.0, List.of()), rep(0.7, 4000, 45.0, List.of()),
                rep(0.7, 4000, 45.0, List.of()))).fadeFromRep()).isNull();
    }

    @Test
    void emgSummaryComparesLeftAndRightAndTheTargetShare() {
        SetAnalyzer.Emg emg = SetAnalyzer.analyzeEmg(List.of(
                new ExerciseSetRequest.Muscle("대둔근", "left", List.of(0.30, 0.30)),
                new ExerciseSetRequest.Muscle("대둔근", "right", List.of(0.40, 0.40)),
                new ExerciseSetRequest.Muscle("대퇴사두근", "left", List.of(0.65, 0.65)),
                new ExerciseSetRequest.Muscle("대퇴사두근", "right", List.of(0.65, 0.65))), "대둔근", true);

        assertThat(emg.simulated()).isTrue();
        assertThat(emg.targetSharePct()).isEqualTo(35.0);
        assertThat(emg.asymmetries()).hasSize(2);
        assertThat(emg.asymmetries().get(0)).isEqualTo(new SetAnalyzer.Asymmetry("대둔근", 0.3, 0.4, 25.0, "right"));
        assertThat(SetAnalyzer.analyzeEmg(null, "대둔근", false)).isNull();
    }

    private static ExerciseSetRequest.Rep rep(double depth, int durationMs, Double lean, List<String> findings) {
        return new ExerciseSetRequest.Rep("side", depth, durationMs, lean, 10.0, null, null, findings);
    }
}
