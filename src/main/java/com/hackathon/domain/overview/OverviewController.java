package com.hackathon.domain.overview;

import com.hackathon.domain.bodyalert.dto.BodyAlertResponse;
import com.hackathon.domain.bodyalert.service.BodyAlertService;
import com.hackathon.domain.exerciseset.dto.ExerciseSetResponse;
import com.hackathon.domain.exerciseset.service.ExerciseSetService;
import com.hackathon.domain.inbody.dto.InbodyResponse;
import com.hackathon.domain.inbody.service.InbodyService;
import com.hackathon.domain.insight.dto.InsightResponse;
import com.hackathon.domain.insight.service.InsightService;
import com.hackathon.domain.survey.dto.SurveyResponse;
import com.hackathon.domain.survey.service.SurveyService;
import com.hackathon.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 화면이 처음 뜰 때 필요한 것을 한 번에 준다. 배포 서버가 미국 동부에 있어 호출 한 번이 0.6초쯤 걸리므로,
 * 화면마다 두세 번 부르던 것을 한 번으로 줄이려고 만들었다.
 */
@Tag(name = "Overview", description = "화면 첫 로딩용. 세트, 패턴, 인바디, 문진, 사전 분석을 한 번에 조회")
@RestController
@RequiredArgsConstructor
public class OverviewController {

    private final ExerciseSetService exerciseSetService;
    private final InsightService insightService;
    private final InbodyService inbodyService;
    private final SurveyService surveyService;
    private final BodyAlertService bodyAlertService;

    public record Overview(
            @Schema(description = "카메라 세트. 최근 것부터")
            List<ExerciseSetResponse> sets,
            @Schema(description = "마지막 패턴 분석 결과")
            List<InsightResponse> insights,
            @Schema(description = "최신 인바디. 없으면 null")
            InbodyResponse inbody,
            @Schema(description = "최신 문진. 없으면 null")
            SurveyResponse survey,
            @Schema(description = "마지막 사전 분석. 없으면 null")
            BodyAlertResponse bodyAlert) {
    }

    @Operation(summary = "한 번에 조회", description = "AI를 부르지 않는다. 없는 항목은 null이나 빈 배열로 온다")
    @GetMapping("/api/overview")
    public ApiResponse<Overview> overview(@RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        List<InbodyResponse> inbody = inbodyService.findMine(userId);
        return ApiResponse.ok(new Overview(
                exerciseSetService.findMine(userId),
                insightService.findMine(userId),
                inbody.isEmpty() ? null : inbody.get(0),
                surveyService.findLatestOrNull(userId),
                bodyAlertService.findLatestOrNull(userId)));
    }
}
