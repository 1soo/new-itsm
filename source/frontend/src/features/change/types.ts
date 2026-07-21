/* change(CHG) 도메인 타입 — api_spec/change.md 계약 기준. */

export type ChangeType = "STANDARD" | "NORMAL" | "EMERGENCY";
export type Risk = "HIGH" | "MEDIUM" | "LOW";
export type ChangeStatus =
  | "REQUESTED"
  | "REVIEW"
  | "PLANNING"
  | "APPROVAL"
  | "IMPLEMENTATION"
  | "CLOSED";

/** 전이 목표 상태(REQUESTED는 시작 상태라 목표에 없음). */
export type ChangeTargetStatus = Exclude<ChangeStatus, "REQUESTED">;

export type Outcome = "SUCCESS" | "FAILURE";
export type LinkTargetType = "INCIDENT" | "PROBLEM";
export type LinkedItemType = "INCIDENT" | "PROBLEM" | "ASSET" | "COMPLIANCE_REQUIREMENT";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}

export interface ChangeSummary {
  id: number;
  ticketKey: string;
  summary: string;
  type: ChangeType;
  status: ChangeStatus;
  risk: Risk | null;
  scheduledAt: string | null;
  updatedAt: string;
  /** 진행 중인 승인 인스턴스의 targetState(원본 코드값, 없으면 null). 2026-07-22 유지보수 요청 신규. */
  pendingApprovalTargetState: ChangeStatus | null;
}

export interface ChangeResult {
  outcome: Outcome | null;
  rolledBack: boolean;
  note: string;
}

export interface ChangeLink {
  type: LinkedItemType;
  targetKey: string;
}

/** 승인 프로세스 커스텀 기능(유지보수 요청) — approvalRequestId=null이면 매칭되는 승인 프로세스가 없어 게이트 없이 진행. */
export interface ChangeApproval {
  approvalRequestId: number | null;
  status: "IN_PROGRESS" | "APPROVED" | "REJECTED" | null;
  /** 원본 코드값(도착 상태, 생성 시점 스냅샷). 2026-07-22 유지보수 요청 신규. */
  targetState: ChangeStatus | null;
}

export interface ChangeDetail {
  id: number;
  ticketKey: string;
  summary: string;
  description: string;
  type: ChangeType;
  risk: Risk | null;
  status: ChangeStatus;
  implementationPlan: string;
  rollbackPlan: string;
  result: ChangeResult;
  approval: ChangeApproval;
  links: ChangeLink[];
  /** BE 제공 시 이 목록만 전이 버튼으로 노출. */
  allowedTransitions?: ChangeTargetStatus[];
}

export interface CreateChangeInput {
  summary: string;
  description?: string;
  type: ChangeType;
  risk?: Risk;
  implementationPlan?: string;
  affectedSystems?: string[];
  rollbackPlan?: string;
  scheduledAt?: string;
  templateId?: number;
}

export interface CreatedChange {
  id: number;
  ticketKey: string;
  status: ChangeStatus;
  type: ChangeType;
}

export interface ChangeListQuery {
  type?: ChangeType;
  status?: ChangeStatus;
  risk?: Risk;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface ClassificationInput {
  type: ChangeType;
  risk?: Risk;
}

export interface ClassificationResult {
  id: number;
  type: ChangeType;
  risk: Risk | null;
}

export interface ResultInput {
  outcome: Outcome;
  rolledBack: boolean;
  note?: string;
}

export interface LinkInput {
  targetType: LinkTargetType;
  targetId: number;
}

export interface ScheduleItem {
  id: number;
  ticketKey: string;
  summary: string;
  type: ChangeType;
  scheduledAt: string;
}

export interface ChangeTemplate {
  id: number;
  name: string;
  description: string;
}

export interface ChangeMetrics {
  successRate: number;
  failureRate: number;
  emergencyRate: number;
  total: number;
}
