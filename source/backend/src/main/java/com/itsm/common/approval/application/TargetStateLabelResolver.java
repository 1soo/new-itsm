package com.itsm.common.approval.application;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 승인 대상 상태(targetState) 코드값 → 표시 라벨 공용 리졸버(2026-07-22 상태별 승인자 지정 확장).
 * 9개 도메인의 상태 enum 라벨을 정적 맵으로 보유한다(각 FE `features/{domain}/status.ts`의
 * STATUS_LABEL과 동일 값 — 도메인 상태 enum 자체에 label()이 없는 도메인이 대부분이라 여기서 별도 관리).
 * API-COM-003/004 응답의 targetStateLabel과 API-AUTH-031(관리자 상태 옵션 조회)이 함께 재사용한다.
 */
@Component
public class TargetStateLabelResolver {

    private static final Map<String, Map<String, String>> LABELS_BY_DOMAIN = new LinkedHashMap<>();

    static {
        LABELS_BY_DOMAIN.put("SERVICE_REQUEST", orderedMap(
                "SUBMITTED", "제출됨",
                "VALIDATED", "검증됨",
                "ROUTED", "라우팅됨",
                "IN_FULFILLMENT", "이행 중",
                "FULFILLED", "이행 완료",
                "CLOSED", "종료"));
        LABELS_BY_DOMAIN.put("CHANGE", orderedMap(
                "REQUESTED", "요청",
                "REVIEW", "검토",
                "PLANNING", "계획",
                "APPROVAL", "승인",
                "IMPLEMENTATION", "구현",
                "CLOSED", "종료"));
        LABELS_BY_DOMAIN.put("KNOWLEDGE", orderedMap(
                "DRAFT", "초안",
                "IN_REVIEW", "검토",
                "PUBLISHED", "게시"));
        LABELS_BY_DOMAIN.put("INCIDENT", orderedMap(
                "NEW", "신규",
                "IN_PROGRESS", "대응중",
                "RESOLVED", "해결",
                "CLOSED", "종료"));
        LABELS_BY_DOMAIN.put("PROBLEM", orderedMap(
                "DETECTION", "탐지",
                "CLASSIFICATION", "분류",
                "INVESTIGATION", "조사중",
                "KNOWN_ERROR", "알려진 오류",
                "WORKAROUND", "워크어라운드",
                "RESOLVED_CLOSED", "종료"));
        LABELS_BY_DOMAIN.put("ASSET", orderedMap(
                "PLANNING", "계획",
                "PROCUREMENT", "구매",
                "OPERATION", "운영",
                "MAINTENANCE", "유지보수",
                "RETIREMENT", "폐기"));
        LABELS_BY_DOMAIN.put("VULNERABILITY", orderedMap(
                "DISCOVERY", "발견",
                "ASSESSMENT", "평가",
                "PRIORITIZATION", "우선순위 산정",
                "REMEDIATION", "개선",
                "VERIFICATION", "검증",
                "REPORTING", "보고"));
        LABELS_BY_DOMAIN.put("COMPLIANCE", orderedMap(
                "DETECTED", "탐지",
                "IN_PROGRESS", "조치중",
                "RESOLVED", "해결"));
        LABELS_BY_DOMAIN.put("ESM", orderedMap(
                "SUBMITTED", "제출됨",
                "IN_PROGRESS", "처리중",
                "COMPLETED", "완료",
                "REJECTED", "반려"));
    }

    private static Map<String, String> orderedMap(String... keyValuePairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return map;
    }

    /** targetState 원본 코드에 대응하는 표시 라벨. 도메인·상태가 정적 맵에 없으면 원본 코드를 그대로 반환. */
    public String label(String domain, String targetState) {
        if (domain == null || targetState == null) {
            return targetState;
        }
        return LABELS_BY_DOMAIN.getOrDefault(domain, Map.of()).getOrDefault(targetState, targetState);
    }

    /** 도메인의 전체 상태값(순서 보장) → 라벨 맵. 정의되지 않은 도메인이면 빈 맵. */
    public Map<String, String> statesOf(String domain) {
        return LABELS_BY_DOMAIN.getOrDefault(domain, Map.of());
    }
}
