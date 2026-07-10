package com.itsm.esm.domain;

/**
 * 카탈로그 항목의 체크리스트 템플릿 유형. 요청 제출 시(API-ESM-005) ONBOARDING/OFFBOARDING이면 체크리스트가 자동 생성된다.
 */
public enum ChecklistTemplateType {
    NONE,
    ONBOARDING,
    OFFBOARDING
}
