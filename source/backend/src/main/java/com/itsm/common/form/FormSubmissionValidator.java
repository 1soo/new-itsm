package com.itsm.common.form;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SRM/ESM 공용 동적 폼(form.io) 제출 데이터 서버 재검증기.
 * form_schema(Form.io Form JSON)의 input:true 리프 컴포넌트를 재귀 수집해 required/minLength/maxLength/min/max/pattern을 검증한다
 * (docs/02_plan/api_spec/common.md 0-2절, docs/source/form_io/component-schema-and-validation.md 3절).
 * conditional/calculateValue/custom은 해석하지 않는다(범위 밖).
 */
public final class FormSubmissionValidator {

    private FormSubmissionValidator() {
    }

    @SuppressWarnings("unchecked")
    public static void validate(Map<String, Object> formSchema, Map<String, Object> formValues) {
        List<Map<String, Object>> leaves = new ArrayList<>();
        Object components = formSchema == null ? null : formSchema.get("components");
        collectLeaves(components, leaves);

        Map<String, Object> values = formValues == null ? Map.of() : formValues;
        for (Map<String, Object> leaf : leaves) {
            validateLeaf(leaf, values);
        }
    }

    @SuppressWarnings("unchecked")
    private static void collectLeaves(Object node, List<Map<String, Object>> leaves) {
        if (node instanceof List<?> list) {
            for (Object item : list) {
                collectLeaves(item, leaves);
            }
        } else if (node instanceof Map<?, ?> raw) {
            Map<String, Object> component = (Map<String, Object>) raw;
            Object nestedComponents = component.get("components");
            Object nestedColumns = component.get("columns");
            Object nestedRows = component.get("rows");
            boolean hasChildren = nestedComponents != null || nestedColumns != null || nestedRows != null;
            boolean isInput = !(component.get("input") instanceof Boolean b) || b;

            if (isInput && !hasChildren) {
                leaves.add(component);
            }
            collectLeaves(nestedComponents, leaves);
            collectLeaves(nestedColumns, leaves);
            collectLeaves(nestedRows, leaves);
        }
    }

    @SuppressWarnings("unchecked")
    private static void validateLeaf(Map<String, Object> component, Map<String, Object> values) {
        Object keyObj = component.get("key");
        if (!(keyObj instanceof String key)) {
            return;
        }
        String label = component.get("label") instanceof String s ? s : key;
        Map<String, Object> rules = component.get("validate") instanceof Map<?, ?> v
                ? (Map<String, Object>) v : Map.of();
        Object value = values.get(key);
        boolean empty = value == null || (value instanceof String s && s.isBlank());

        if (Boolean.TRUE.equals(rules.get("required")) && empty) {
            throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING, "필수 항목 누락: " + label);
        }
        if (empty) {
            return;
        }

        if (value instanceof String str) {
            Integer minLength = intOf(rules.get("minLength"));
            if (minLength != null && str.length() < minLength) {
                throw invalid(label, "최소 길이(" + minLength + "자) 미만입니다.");
            }
            Integer maxLength = intOf(rules.get("maxLength"));
            if (maxLength != null && str.length() > maxLength) {
                throw invalid(label, "최대 길이(" + maxLength + "자)를 초과했습니다.");
            }
            if (rules.get("pattern") instanceof String pattern && !pattern.isBlank() && !str.matches(pattern)) {
                throw invalid(label, "형식이 올바르지 않습니다.");
            }
        }

        Double numeric = numericOf(value);
        if (numeric != null) {
            Double min = doubleOf(rules.get("min"));
            if (min != null && numeric < min) {
                throw invalid(label, "최소값(" + min + ") 미만입니다.");
            }
            Double max = doubleOf(rules.get("max"));
            if (max != null && numeric > max) {
                throw invalid(label, "최대값(" + max + ")을 초과했습니다.");
            }
        }
    }

    private static BusinessException invalid(String label, String reason) {
        return new BusinessException(ErrorCode.FORM_FIELD_INVALID, label + ": " + reason);
    }

    private static Integer intOf(Object o) {
        return o instanceof Number n ? n.intValue() : null;
    }

    private static Double doubleOf(Object o) {
        return o instanceof Number n ? n.doubleValue() : null;
    }

    private static Double numericOf(Object o) {
        if (o instanceof Number n) {
            return n.doubleValue();
        }
        if (o instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
