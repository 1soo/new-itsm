package com.itsm.compliance.domain;

/**
 * 준수 상태. 저장 컬럼이 아니라 시정조치 미해결(DETECTED/IN_PROGRESS) 존재 여부로 조회 시점에 계산한다.
 */
public enum ComplianceStatus {
    COMPLIANT,
    NON_COMPLIANT
}
