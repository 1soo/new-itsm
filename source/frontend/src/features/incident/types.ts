/* incident(INC) 도메인 타입 — api_spec/incident.md 계약 기준. */

export type Severity = "SEV1" | "SEV2" | "SEV3";
export type Priority = "P1" | "P2" | "P3" | "P4";
export type IncidentStatus = "NEW" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";
export type IncidentTargetStatus = "IN_PROGRESS" | "RESOLVED" | "CLOSED";
export type ResponderRole = "TECH_LEAD" | "COMMS" | "SCRIBE";
export type Visibility = "INTERNAL" | "EXTERNAL";
export type EscalationType = "HIERARCHICAL" | "FUNCTIONAL";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}

export interface IncidentSummary {
  id: number;
  ticketKey: string;
  summary: string;
  severity: Severity;
  status: IncidentStatus;
  assignee?: string;
  postmortemRequired: boolean;
  updatedAt: string;
  /** 진행 중인 승인 인스턴스의 targetState(원본 코드값, 없으면 null). 2026-07-22 유지보수 요청 신규. */
  pendingApprovalTargetState: IncidentStatus | null;
}

export interface Responder {
  userId: number;
  name: string;
  role: ResponderRole;
}

export interface IncidentMetricsDetail {
  mttdMinutes: number | null;
  mttaMinutes: number | null;
  mttrMinutes: number | null;
}

export interface IncidentLink {
  type: "PROBLEM" | "ASSET";
  targetKey: string;
}

export interface IncidentTimelineEvent {
  type: string;
  visibility: Visibility;
  message: string;
  at: string;
  actor: string;
}

/** 승인 프로세스 커스텀 기능(유지보수 요청) — approvalRequestId=null이면 매칭되는 승인 프로세스가 없어 게이트 없이 진행. */
export interface IncidentApproval {
  approvalRequestId: number | null;
  status: "IN_PROGRESS" | "APPROVED" | "REJECTED" | null;
  /** 원본 코드값(도착 상태, 생성 시점 스냅샷). 2026-07-22 유지보수 요청 신규. */
  targetState: IncidentStatus | null;
}

export interface IncidentDetail {
  id: number;
  ticketKey: string;
  summary: string;
  description: string;
  severity: Severity;
  priority: Priority;
  status: IncidentStatus;
  affectedService?: string;
  affectedProduct?: string;
  responders: Responder[];
  metrics: IncidentMetricsDetail;
  approval: IncidentApproval;
  links: IncidentLink[];
  timeline: IncidentTimelineEvent[];
  /** 목록 배너 판단용(제공 시). */
  postmortemRequired?: boolean;
  /** BE 제공 시 이 목록만 전이 버튼으로 노출. */
  allowedTransitions?: IncidentTargetStatus[];
}

export interface CreateIncidentInput {
  summary: string;
  description?: string;
  severity: Severity;
  affectedService?: string;
  affectedProduct?: string;
}

export interface CreatedIncident {
  id: number;
  ticketKey: string;
  status: IncidentStatus;
}

export interface ResolveInput {
  impactStartAt?: string;
  detectedAt?: string;
  impactEndAt?: string;
  resolutionNote?: string;
}

export interface IncidentListQuery {
  status?: IncidentStatus | "";
  severity?: Severity | "";
  assignee?: string;
  keyword?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

// 포스트모템
export interface ActionItem {
  description: string;
  owner: string;
  dueDate?: string;
  status: "OPEN" | "DONE";
}

export interface Postmortem {
  summary?: string;
  timeline?: string;
  fiveWhys: string[];
  rootCause: string;
  actionItems: ActionItem[];
}

export interface IncidentMetrics {
  count: number;
  severityDistribution: { SEV1: number; SEV2: number; SEV3: number };
  avgMttrMinutes: number;
}
