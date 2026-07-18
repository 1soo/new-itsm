package com.itsm.common.form;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;

import java.util.Map;

/**
 * SRM 동적 폼(8×n 그리드) formSchema/formValues의 Map&lt;String,Object&gt; ↔ JSON 문자열 직렬화 유틸.
 * ServiceCatalogService/ServiceRequestService가 각자 구현하던 중복 로직을 추출함(2026-07-18 코드 리뷰).
 */
public final class FormJsonMapper {

    private FormJsonMapper() {
    }

    public static String writeJson(ObjectMapper objectMapper, Map<String, Object> value,
                                    String defaultJson, String errorMessage) {
        if (value == null) {
            return defaultJson;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, errorMessage);
        }
    }

    public static Map<String, Object> readJsonMap(ObjectMapper objectMapper, String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
