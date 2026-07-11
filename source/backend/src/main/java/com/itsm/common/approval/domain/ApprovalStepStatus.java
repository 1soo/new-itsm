package com.itsm.common.approval.domain;

/**
 * 승인 인스턴스 차수(approval_request_step) 상태. SKIPPED는 이전 차수 반려로 이후 차수가 진행되지 않음을 뜻한다.
 */
public enum ApprovalStepStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SKIPPED
}
