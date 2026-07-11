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
