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

export interface CorrectiveAction {
  id: number;
  description: string;
  status: CorrectiveActionStatus;
  updatedAt: string;
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
