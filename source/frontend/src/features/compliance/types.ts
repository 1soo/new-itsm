/* compliance(COMP) 도메인 타입 — api_spec/compliance.md 계약 기준. */

export type ComplianceStatus = "COMPLIANT" | "NON_COMPLIANT";
export type CorrectiveActionStatus = "DETECTED" | "IN_PROGRESS" | "RESOLVED";
/** 전이 목표 상태(DETECTED는 시작 상태라 목표에 없음). */
export type CorrectiveActionTargetStatus = Exclude<CorrectiveActionStatus, "DETECTED">;

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}

export interface RequirementSummary {
  id: number;
  requirementKey: string;
  name: string;
  basis: string;
  owner: string | null;
  complianceStatus: ComplianceStatus;
  updatedAt: string;
}

/** 승인 프로세스 커스텀 기능(유지보수 요청) — approvalRequestId=null이면 매칭되는 승인 프로세스가 없어 게이트 없이 진행. 시정조치 개별로 붙는다(요구사항당 여러 건 가능). */
export interface ComplianceApproval {
  approvalRequestId: number | null;
  status: "IN_PROGRESS" | "APPROVED" | "REJECTED" | null;
  /** 원본 코드값(도착 상태, 생성 시점 스냅샷). 2026-07-22 유지보수 요청 신규. */
  targetState: CorrectiveActionStatus | null;
}

export interface CorrectiveAction {
  id: number;
  description: string;
  status: CorrectiveActionStatus;
  updatedAt: string;
  approval: ComplianceApproval;
}

export interface LinkedChange {
  id: number;
  ticketKey: string;
}

export interface RequirementDetail {
  id: number;
  requirementKey: string;
  name: string;
  basis: string;
  scope: string | null;
  owner: string | null;
  complianceStatus: ComplianceStatus;
  correctiveActions: CorrectiveAction[];
  linkedChanges: LinkedChange[];
}

export interface RequirementListQuery {
  complianceStatus?: ComplianceStatus;
  ownerAssigned?: boolean;
  page?: number;
  size?: number;
}

export interface CreateRequirementInput {
  name: string;
  basis: string;
  scope?: string;
}

export interface CreatedRequirement {
  id: number;
  requirementKey: string;
}

export interface UpdateRequirementInput {
  name: string;
  basis: string;
  scope?: string;
}

export interface CorrectiveActionInput {
  description: string;
}

export interface CreatedCorrectiveAction {
  id: number;
  status: CorrectiveActionStatus;
}

export interface ComplianceAuditLog {
  eventType: string;
  actor: string;
  target: string;
  result: "SUCCESS" | "FAILURE";
  occurredAt: string;
}

export interface ComplianceAuditLogQuery {
  requirementId?: number;
  from?: string;
  to?: string;
}

export interface ComplianceMetrics {
  totalRequirements: number;
  compliantCount: number;
  nonCompliantCount: number;
  openCorrectiveActionCount: number;
  complianceRate: number;
}
