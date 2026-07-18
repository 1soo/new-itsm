package com.itsm.common.form;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;

import java.util.List;
import java.util.Map;

/**
 * SRM 전용 8×n 그리드 폼 제출 데이터 서버 재검증기.
 * form_schema.components는 중첩 레이아웃이 없는 평면 배열이라 재귀 순회가 불필요하다
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

        if (validation.get("regex") instanceof String regex && !regex.isBlank()
                && value instanceof String str && !str.matches(regex)) {
            throw new BusinessException(ErrorCode.FORM_FIELD_INVALID, label + ": 형식이 올바르지 않습니다.");
        }
    }
}
