package com.itsm.common.form;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;

import java.util.List;
import java.util.Map;

/**
 * SRM 전용 8×n 그리드 폼 제출 데이터 서버 재검증기.
 * form_schema.components는 중첩 레이아웃이 없는 평면 배열이라 재귀 순회가 불필요하다.
 * 배열 순서대로 순회하며 첫 번째로 위반이 발견되는 컴포넌트에서 즉시 예외를 던진다(여러 위반을 모아 반환하지 않음,
 * FE의 순차 1건 표시와 동일한 계약). type=guide-text/guide-file은 값이 없는 정적 컴포넌트라 검증 대상에서 제외한다
 * (label 타입은 그리드 배치 컴포넌트에서 폐기되고 컴포넌트에 부여하는 라벨(태그)로 대체됐으나, 스킵 조건에 남은 "label" 분기는
 * 무해하여 제거하지 않았다). validation.regex는 type=text에만 평가한다(다른 타입에 값이 있어도 평가하지 않음)
 * (docs/02_plan/api_spec/common.md 0-2절, docs/02_plan/api_spec/service-request.md API-SRM-002).
 */
public final class FormSubmissionValidator {

    private FormSubmissionValidator() {
    }

    @SuppressWarnings("unchecked")
    public static void validate(Map<String, Object> formSchema, Map<String, Object> formValues) {
        Object componentsObj = formSchema == null ? null : formSchema.get("components");
        if (!(componentsObj instanceof List<?> components)) {
            return;
        }
        Map<String, Object> values = formValues == null ? Map.of() : formValues;
        for (Object componentObj : components) {
            if (componentObj instanceof Map<?, ?> raw) {
                validateComponent((Map<String, Object>) raw, values);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void validateComponent(Map<String, Object> component, Map<String, Object> values) {
        Object type = component.get("type");
        if ("label".equals(type) || "guide-text".equals(type) || "guide-file".equals(type)) {
            return;
        }
        Object keyObj = component.get("key");
        if (!(keyObj instanceof String key)) {
            return;
        }
        String label = component.get("label") instanceof String s ? s : key;
        Map<String, Object> validation = component.get("validation") instanceof Map<?, ?> v
                ? (Map<String, Object>) v : Map.of();
        Object value = values.get(key);
        boolean empty = value == null || (value instanceof String s && s.isBlank());

        if (Boolean.TRUE.equals(validation.get("required")) && empty) {
            throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING, "필수 항목 누락: " + label);
        }
        if (empty) {
            return;
        }

        if ("text".equals(type) && validation.get("regex") instanceof String regex && !regex.isBlank()
                && value instanceof String str && !str.matches(regex)) {
            throw new BusinessException(ErrorCode.FORM_FIELD_INVALID, label + ": 형식이 올바르지 않습니다.");
        }
    }
}
