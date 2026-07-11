package com.itsm.common.exception;

/**
 * 도메인/비즈니스 예외. 전역 예외 처리기에서 표준 오류 응답으로 변환한다.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Long approvalRequestId;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.approvalRequestId = null;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.approvalRequestId = null;
    }

    /** 승인 게이트 차단(409)에서 생성/기존 인스턴스 id를 응답에 함께 실어 보낼 때 사용(common.md 0절). */
    public BusinessException(ErrorCode errorCode, String message, Long approvalRequestId) {
        super(message);
        this.errorCode = errorCode;
        this.approvalRequestId = approvalRequestId;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Long getApprovalRequestId() {
        return approvalRequestId;
    }
}
