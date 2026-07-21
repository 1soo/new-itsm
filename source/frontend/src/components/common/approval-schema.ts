import type { TFunction } from "i18next";

import type { StatusTone } from "@/components/common/status-badge";

/**
 * 승인 프로세스 공용 타입 — API-COM-004(`docs/02_plan/api_spec/common.md`) 응답 구조와 필드명을 그대로 따른다.
 * `approval-step-progress.tsx`(승인 대기함 상세·도메인 상세 패널 공용)가 참조하며,
 * `approval-process-flow.tsx`(admin.md SCR-ADMIN-008)는 `ApprovalMatchType`만 별도로 공유한다.
 */
export type ApprovalMatchType = "AND" | "OR";

export type ApprovalStepDecisionMode = ApprovalMatchType;
export type ApprovalStepStatus = "PENDING" | "APPROVED" | "REJECTED" | "SKIPPED";
export type ApprovalRoleDecisionValue = "PENDING" | "APPROVE" | "REJECT";

export interface ApprovalStepRole {
  roleCode: string;
  roleName: string;
  decision: ApprovalRoleDecisionValue;
  decidedBy: string | null;
  reason: string | null;
  decidedAt: string | null;
}

export interface ApprovalStep {
  stepNo: number;
  decisionMode: ApprovalStepDecisionMode;
  status: ApprovalStepStatus;
  roles: ApprovalStepRole[];
}

/** 승인 인스턴스(ApprovalRequest) 상태(API-COM-004 status). */
export type ApprovalRequestStatus = "IN_PROGRESS" | "APPROVED" | "REJECTED";

export interface ApprovalStatusDisplay {
  tone: StatusTone;
  label: string;
  /** REJECTED일 때만 true — 재승인요청 버튼(API-COM-006, `ApprovalPanel`) 노출 플래그. */
  showResubmit: boolean;
}

/**
 * 승인 프로세스 커스텀 기능의 파생 상태 표시 공용 헬퍼(2026-07-22 유지보수 요청, 확정 방침 2).
 * 9개 도메인 상세/목록 페이지가 자체 상태 배지(`base`)에 열린 승인요청(IN_PROGRESS)/반려(REJECTED)
 * 여부를 얹어 파생 표시한다 — 도메인 status enum 자체는 변경하지 않고 조회 시점에만 파생한다.
 * `status`가 null(매칭 규칙 없음/미평가) 또는 APPROVED면 `base`를 그대로 반환한다.
 */
export function deriveApprovalStatusDisplay(
  t: TFunction,
  base: { tone: StatusTone; label: string },
  opts: { status: ApprovalRequestStatus | null; targetStateLabel: string | null },
): ApprovalStatusDisplay {
  if (opts.status === "IN_PROGRESS") {
    return {
      tone: "warning",
      label: t("approval.pendingStatusLabel", {
        ns: "common",
        targetState: opts.targetStateLabel,
        defaultValue: `${opts.targetStateLabel}(승인대기)`,
      }),
      showResubmit: false,
    };
  }
  if (opts.status === "REJECTED") {
    return {
      tone: "danger",
      label: t("approval.rejectedStatusLabel", {
        ns: "common",
        targetState: opts.targetStateLabel,
        defaultValue: `${opts.targetStateLabel}(반려됨)`,
      }),
      showResubmit: true,
    };
  }
  return { ...base, showResubmit: false };
}
