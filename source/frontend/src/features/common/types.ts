/* common(알림 확인처리 · 전 도메인 공용 승인) 도메인 타입 — api_spec/common.md 기준. */

// 차수·역할별 결정 구조(API-COM-004 steps)는 components/common의 공용 계약을 그대로 재사용한다
// (ApprovalStepProgress/ApprovalPanel과 필드명이 어긋나지 않도록 단일 원천 유지).
import type { ApprovalRequestStatus, ApprovalStep, ApprovalStepStatus } from "@/components/common";
export type { ApprovalStep, ApprovalStepStatus };

/** API-COM-001/002: 확인처리 대상 알림 유형. APPROVAL은 전 도메인 공용 승인 대기(승인 프로세스 커스텀 기능). */
export type NotificationType = "APPROVAL" | "ASSET_EXPIRY";

export interface DismissalItem {
  notificationType: NotificationType;
  sourceId: number;
}

export interface DismissResult {
  dismissedCount: number;
}

export interface NotificationDismissal {
  notificationType: NotificationType;
  sourceId: number;
  dismissedAt: string;
}

export interface NotificationDismissalListResponse {
  items: NotificationDismissal[];
}

/* 전 도메인 공용 승인 대기함/결정(SCR-COM-014) — API-COM-003~005 기준. */

/**
 * 승인 대상 티켓 유형. 승인 프로세스 대상 9개 도메인과 동일 값 체계를 기본으로 하되,
 * ESM/COMPLIANCE는 승인 인스턴스(ApprovalRequest.ticketType)가 도메인보다 세분화된 값
 * (ESM_REQUEST: 부서요청 단위 / CORRECTIVE_ACTION: 시정조치 단위)을 그대로 사용한다.
 * 대기함 도메인 필터는 이 값이 아니라 approval_process.domain 기준이라 영향 없다.
 */
export type TicketType =
  | "SERVICE_REQUEST"
  | "CHANGE"
  | "KNOWLEDGE"
  | "INCIDENT"
  | "PROBLEM"
  | "ASSET"
  | "VULNERABILITY"
  | "COMPLIANCE"
  | "ESM"
  | "ESM_REQUEST"
  | "CORRECTIVE_ACTION";

export interface ApprovalListItem {
  approvalRequestId: number;
  ticketType: TicketType;
  ticketId: number;
  ticketKey: string;
  ticketSummary: string;
  /** 원본 코드값(도착 상태). */
  targetState: string;
  /** 표시명(백엔드 resolve, 2026-07-22 유지보수 요청). */
  targetStateLabel: string;
  requester: string;
  currentStepNo: number;
  requestedAt: string;
}

export type { ApprovalRequestStatus };

export interface ApprovalDetail {
  id: number;
  ticketType: TicketType;
  ticketId: number;
  ticketKey: string;
  /** 원본 코드값(도착 상태, 생성 시점 스냅샷). */
  targetState: string;
  targetStateLabel: string;
  status: ApprovalRequestStatus;
  currentStepNo: number | null;
  steps: ApprovalStep[];
}

export interface ApprovalListQuery {
  domain?: TicketType | "";
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}

export interface ApprovalDecisionRequest {
  decision: "APPROVE" | "REJECT";
  reason?: string;
}

export interface ApprovalDecisionResult {
  approvalRequestId: number;
  stepNo: number;
  stepStatus: ApprovalStepStatus;
  requestStatus: ApprovalRequestStatus;
}

/** 반려 후 재승인요청(API-COM-006, 2026-07-22 유지보수 요청). */
export interface ApprovalResubmitRequest {
  ticketType: TicketType;
  ticketId: number;
}

export interface ApprovalResubmitResult {
  /** NO_RULE_MATCHED(매칭 규칙 소멸)면 인스턴스를 생성하지 않아 응답에서 생략(백엔드 `default-property-inclusion: non_null`). */
  approvalRequestId?: number | null;
  ticketType: TicketType;
  ticketId: number;
  targetState: string;
  /** IN_PROGRESS(새 인스턴스 생성) 또는 NO_RULE_MATCHED(매칭 규칙 소멸, 승인 없이 통과 가능). */
  status: "IN_PROGRESS" | "NO_RULE_MATCHED";
  currentStepNo?: number | null;
}
