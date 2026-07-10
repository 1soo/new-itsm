package com.itsm.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 표준 오류 코드. API 명세서(api_spec/auth.md)의 응답 코드 규칙과 일치시킨다.
 */
public enum ErrorCode {

    // 400
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력 형식이 올바르지 않습니다."),
    PASSWORD_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "새 비밀번호가 정책을 위반했습니다."),
    ROLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 역할입니다."),

    // 401
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었거나 무효화되었습니다. 다시 로그인하세요."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다."),

    // 403
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "비활성화된 계정입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."),

    // 415
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다."),

    // --- Service Request (SRM) ---
    CATALOG_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "카탈로그 항목을 찾을 수 없습니다."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "서비스 요청을 찾을 수 없습니다."),
    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "승인 정보를 찾을 수 없습니다."),
    ASSIGNEE_NOT_FOUND(HttpStatus.NOT_FOUND, "배정 대상 사용자를 찾을 수 없습니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "허용되지 않은 상태 전이입니다."),
    REQUEST_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "이미 종료된 요청입니다."),
    REJECT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "반려 사유는 필수입니다."),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "필수 양식 필드가 누락되었습니다."),
    CSAT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "종료된 요청에만 CSAT를 제출할 수 있습니다."),
    CSAT_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "이미 CSAT가 제출되었습니다."),
    APPROVAL_PENDING(HttpStatus.CONFLICT, "승인 대기 중에는 이행할 수 없습니다."),
    APPROVAL_ALREADY_DECIDED(HttpStatus.CONFLICT, "이미 결정된 승인입니다."),

    // --- Incident (INC) ---
    INCIDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "인시던트를 찾을 수 없습니다."),
    POSTMORTEM_NOT_FOUND(HttpStatus.NOT_FOUND, "포스트모템이 아직 작성되지 않았습니다."),
    ROOT_CAUSE_REQUIRED(HttpStatus.BAD_REQUEST, "근본원인(rootCause)은 필수입니다."),
    ESCALATION_TARGET_NOT_FOUND(HttpStatus.BAD_REQUEST, "에스컬레이션 대상 사용자를 찾을 수 없습니다."),

    // --- Problem (PRB) ---
    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다."),
    PROBLEM_ACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "후속 조치를 찾을 수 없습니다."),
    WORKAROUND_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "워크어라운드 내용은 필수입니다."),
    LINK_TARGET_NOT_FOUND(HttpStatus.BAD_REQUEST, "연계 대상을 찾을 수 없습니다."),
    LINK_TARGET_REQUIRED(HttpStatus.BAD_REQUEST, "연계할 대상 id 또는 신규 생성 지정이 필요합니다."),

    // --- Change (CHG) ---
    CHANGE_NOT_FOUND(HttpStatus.NOT_FOUND, "변경 요청을 찾을 수 없습니다."),
    CHANGE_NOT_APPROVED(HttpStatus.BAD_REQUEST, "승인되지 않은 변경입니다."),

    // --- Knowledge (KM) ---
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "기사를 찾을 수 없습니다."),
    ARTICLE_NOT_PUBLISHED(HttpStatus.BAD_REQUEST, "게시되지 않은 기사입니다."),

    // --- Asset / CI (ITAM) ---
    ASSET_NOT_FOUND(HttpStatus.NOT_FOUND, "자산을 찾을 수 없습니다."),
    CI_NOT_FOUND(HttpStatus.NOT_FOUND, "구성 항목(CI)을 찾을 수 없습니다."),
    CI_SELF_RELATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CI는 자기 자신과 관계를 맺을 수 없습니다."),

    // --- Enterprise Service Management (ESM) ---
    ESM_CATALOG_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "카탈로그 항목을 찾을 수 없습니다."),
    ESM_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "부서 요청을 찾을 수 없습니다."),
    ESM_CHECKLIST_TEMPLATE_REQUIRED(HttpStatus.BAD_REQUEST, "체크리스트 템플릿이 정의되지 않았습니다."),
    TARGET_USER_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "대상자명(targetUserName)은 필수입니다."),
    ESM_HR_CASE_NOT_FOUND(HttpStatus.NOT_FOUND, "HR 케이스를 찾을 수 없습니다."),
    ESM_CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "체크리스트를 찾을 수 없습니다."),
    ESM_CHECKLIST_TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "하위 작업을 찾을 수 없습니다."),

    // --- Vulnerability (VULN) ---
    VULNERABILITY_NOT_FOUND(HttpStatus.NOT_FOUND, "취약점을 찾을 수 없습니다."),

    // --- Compliance (COMP) ---
    COMPLIANCE_REQUIREMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "컴플라이언스 요구사항을 찾을 수 없습니다."),
    CORRECTIVE_ACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "시정조치를 찾을 수 없습니다."),

    // 409
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    ROLE_NAME_DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 역할 코드 또는 역할명입니다."),
    ASSIGNEE_REQUIRED_FOR_REMEDIATION(HttpStatus.CONFLICT, "담당자가 배정되지 않아 개선(REMEDIATION) 단계로 전이할 수 없습니다."),

    // 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
