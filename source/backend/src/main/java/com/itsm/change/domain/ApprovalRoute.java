package com.itsm.change.domain;

/**
 * 승인 경로. AUTO(표준 변경, 승인 자동 통과)/PEER_REVIEW(동료 검토)/CAB(위험도 미평가·고위험 기본 경로).
 */
public enum ApprovalRoute {
    AUTO,
    PEER_REVIEW,
    CAB
}
