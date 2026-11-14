package com.hackathon.domain.inbody.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hackathon.domain.ai.AiDto;
import com.hackathon.domain.ai.AiService;
import com.hackathon.domain.inbody.dto.InbodyRequest;
import com.hackathon.domain.inbody.dto.InbodyResponse;
import com.hackathon.domain.inbody.entity.InbodyRecord;
import com.hackathon.domain.inbody.repository.InbodyRepository;
import com.hackathon.global.error.BusinessException;
import com.hackathon.global.error.ErrorCode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InbodyService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final List<String> NUMBERS = List.of("weightKg", "skeletalMuscleKg", "bodyFatPercent",
            "rightArmKg", "leftArmKg", "trunkKg", "rightLegKg", "leftLegKg");

    private static final String SYSTEM = """
            인바디(체성분 분석) 결과지 사진에서 수치를 읽는다.
            - 체중, 골격근량, 체지방률을 읽는다.
            - 부위별 근육분석(Segmental Lean Analysis)의 오른팔, 왼팔, 몸통, 오른다리, 왼다리 값을 kg로 읽는다. 막대 옆에 적힌 숫자를 쓴다.
            - 읽을 수 없거나 사진에 없는 값은 -1로 둔다. 추측해서 채우지 않는다.
            - 측정일이 보이면 measuredAt에 YYYY-MM-DD로 쓰고, 안 보이면 빈 문자열로 둔다.
            - 사진이 인바디 결과지가 아니면 readable을 false로 두고 나머지는 -1로 둔다.
            """;

    private static final Map<String, Object> SCHEMA = Map.of("properties", Map.of(
            "readable", Map.of("type", "boolean"),
            "measuredAt", Map.of("type", "string"),
            "weightKg", Map.of("type", "number"),
            "skeletalMuscleKg", Map.of("type", "number"),
            "bodyFatPercent", Map.of("type", "number"),
            "rightArmKg", Map.of("type", "number"),
            "leftArmKg", Map.of("type", "number"),
            "trunkKg", Map.of("type", "number"),
            "rightLegKg", Map.of("type", "number"),
            "leftLegKg", Map.of("type", "number")));

    private final InbodyRepository inbodyRepository;
    private final AiService aiService;

    public InbodyResponse create(Long userId, InbodyRequest.Create req) {
        LocalDate today = LocalDate.now(KST);
        if (req.values() != null) {
            InbodyRequest.Values v = req.values();
            return save(InbodyRecord.builder().userId(userId).source("manual")
                    .measuredAt(req.measuredAt() != null ? req.measuredAt() : today)
                    .weightKg(v.weightKg()).skeletalMuscleKg(v.skeletalMuscleKg()).bodyFatPercent(v.bodyFatPercent())
                    .rightArmKg(v.rightArmKg()).leftArmKg(v.leftArmKg()).trunkKg(v.trunkKg())
                    .rightLegKg(v.rightLegKg()).leftLegKg(v.leftLegKg()).build());
        }
        if (req.image() == null) {
            throw new BusinessException(ErrorCode.INBODY_EMPTY);
        }

        JsonNode r = aiService.json(new AiDto.JsonRequest(SYSTEM, "이 인바디 결과지의 수치를 읽어 주세요.",
                null, List.of(req.image()), null, SCHEMA));
        boolean anyNumber = NUMBERS.stream().anyMatch(k -> r.path(k).asDouble(-1) > 0);
        if (!r.path("readable").asBoolean(false) || !anyNumber) {
            throw new BusinessException(ErrorCode.INBODY_NOT_READABLE);
        }
        return save(InbodyRecord.builder().userId(userId).source("photo")
                .measuredAt(req.measuredAt() != null ? req.measuredAt() : parseDate(r.path("measuredAt").asText(""), today))
                .weightKg(number(r, "weightKg")).skeletalMuscleKg(number(r, "skeletalMuscleKg"))
                .bodyFatPercent(number(r, "bodyFatPercent"))
                .rightArmKg(number(r, "rightArmKg")).leftArmKg(number(r, "leftArmKg")).trunkKg(number(r, "trunkKg"))
                .rightLegKg(number(r, "rightLegKg")).leftLegKg(number(r, "leftLegKg")).build());
    }

    public List<InbodyResponse> findMine(Long userId) {
        return inbodyRepository.findByUserIdOrderByMeasuredAtDescIdDesc(userId).stream().map(InbodyResponse::from).toList();
    }

    /** AI 프롬프트에 붙일 최신 인바디 요약. 기록이 없으면 빈 문자열 */
    public String describeLatest(Long userId) {
        List<InbodyRecord> records = inbodyRepository.findByUserIdOrderByMeasuredAtDescIdDesc(userId);
        if (records.isEmpty()) return "";
        InbodyResponse r = InbodyResponse.from(records.get(0));
        StringBuilder sb = new StringBuilder("\n인바디 (").append(r.measuredAt()).append(" 측정, 체성분만 알려 주며 자세는 알 수 없다)\n");
        append(sb, "체중", r.weightKg(), "kg");
        append(sb, "골격근량", r.skeletalMuscleKg(), "kg");
        append(sb, "체지방률", r.bodyFatPercent(), "%");
        append(sb, "오른팔 근육량", r.rightArmKg(), "kg");
        append(sb, "왼팔 근육량", r.leftArmKg(), "kg");
        append(sb, "몸통 근육량", r.trunkKg(), "kg");
        append(sb, "오른다리 근육량", r.rightLegKg(), "kg");
        append(sb, "왼다리 근육량", r.leftLegKg(), "kg");
        if (r.legDiffPct() != null) sb.append("다리 좌우 차이 ").append(r.legDiffPct()).append("%\n");
        if (r.armDiffPct() != null) sb.append("팔 좌우 차이 ").append(r.armDiffPct()).append("%\n");
        return sb.toString();
    }

    private InbodyResponse save(InbodyRecord record) {
        return InbodyResponse.from(inbodyRepository.save(record));
    }

    private static void append(StringBuilder sb, String label, Double value, String unit) {
        if (value != null) sb.append(label).append(' ').append(value).append(unit).append('\n');
    }

    /** 못 읽은 값(-1)과 말이 안 되는 값은 버린다 */
    private static Double number(JsonNode r, String key) {
        double v = r.path(key).asDouble(-1);
        return v > 0 && v < 500 ? v : null;
    }

    private static LocalDate parseDate(String text, LocalDate fallback) {
        try {
            LocalDate d = LocalDate.parse(text);
            return d.isAfter(fallback) ? fallback : d;
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }
}
