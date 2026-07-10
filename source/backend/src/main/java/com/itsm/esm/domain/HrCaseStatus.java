package com.itsm.esm.domain;

/**
 * HR 케이스 상태. 접수(INTAKE)→기록(DOCUMENTATION)→조사(INVESTIGATION)→해결(RESOLUTION) 순차 전이만 허용.
 */
public enum HrCaseStatus {
    INTAKE,
    DOCUMENTATION,
    INVESTIGATION,
    RESOLUTION
}
