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

export type ApprovalRoute = "AUTO" | "PEER_REVIEW" | "CAB";
export type Outcome = "SUCCESS" | "FAILURE";
export type Decision = "APPROVE" | "REJECT";
export type LinkTargetType = "INCIDENT" | "PROBLEM";

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
}

export interface ChangeResult {
  outcome: Outcome | null;
  rolledBack: boolean;
  note: string;
}

export interface ChangeApprovalRecord {
  approver: string;
  decision: "APPROVED" | "REJECTED";
  opinion: string;
  at: string;
}

export interface ChangeLink {
  type: LinkTargetType;
  targetKey: string;
}

export interface ChangeDetail {
  id: number;
  ticketKey: string;
  summary: string;
  description: string;
  type: ChangeType;
  risk: Risk | null;
  status: ChangeStatus;
  approvalRoute: ApprovalRoute;
  implementationPlan: string;
  rollbackPlan: string;
  result: ChangeResult;
  approvals: ChangeApprovalRecord[];
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
  approvalRoute: ApprovalRoute;
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

export interface ApprovalQueueItem {
  changeId: number;
  ticketKey: string;
  type: ChangeType;
  risk: Risk | null;
  requester: string;
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
