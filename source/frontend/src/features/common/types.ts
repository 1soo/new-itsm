/* common(알림 확인처리 · 전 도메인 공용 승인) 도메인 타입 — api_spec/common.md 기준. */

// 차수·역할별 결정 구조(API-COM-004 steps)는 components/common의 공용 계약을 그대로 재사용한다
// (ApprovalStepProgress/ApprovalPanel과 필드명이 어긋나지 않도록 단일 원천 유지).
import type { ApprovalStep, ApprovalStepStatus } from "@/components/common";
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

/** 승인 대상 티켓 유형(승인 프로세스 대상 9개 도메인과 동일 값 체계). */
export type TicketType =
  | "SERVICE_REQUEST"
  | "CHANGE"
  | "KNOWLEDGE"
  | "INCIDENT"
  | "PROBLEM"
  | "ASSET"
  | "VULNERABILITY"
  | "COMPLIANCE"
  | "ESM";

export interface ApprovalListItem {
  approvalRequestId: number;
  ticketType: TicketType;
  ticketId: number;
  ticketKey: string;
  ticketSummary: string;
  requester: string;
  currentStepNo: number;
  requestedAt: string;
}

export type ApprovalRequestStatus = "IN_PROGRESS" | "APPROVED" | "REJECTED";

export interface ApprovalDetail {
  id: number;
  ticketType: TicketType;
  ticketId: number;
  ticketKey: string;
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
