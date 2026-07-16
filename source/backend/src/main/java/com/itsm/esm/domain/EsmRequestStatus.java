package com.itsm.esm.domain;

/**
 * 부서 요청 상태.
 */
public enum EsmRequestStatus {
    SUBMITTED,
    IN_PROGRESS,
    COMPLETED,
    REJECTED;

    /** 상태 한글 라벨(FE `features/esm/status.ts`의 REQUEST_STATUS_LABEL과 동일 값). */
    public String label() {
        return switch (this) {
            case SUBMITTED -> "제출됨";
            case IN_PROGRESS -> "처리중";
            case COMPLETED -> "완료";
            case REJECTED -> "반려";
        };
    }
}
