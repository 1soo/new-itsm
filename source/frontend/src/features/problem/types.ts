/* problem(PRB) 도메인 타입 — api_spec/problem.md 계약 기준. */

export type ProblemStatus =
  | "DETECTION"
  | "CLASSIFICATION"
  | "INVESTIGATION"
  | "KNOWN_ERROR"
  | "WORKAROUND"
  | "RESOLVED_CLOSED";

/** 전이 목표 상태(DETECTION은 시작 상태라 목표에 없음). */
export type ProblemTargetStatus = Exclude<ProblemStatus, "DETECTION">;

export type Origin = "REACTIVE" | "PROACTIVE";
export type Level = "HIGH" | "MEDIUM" | "LOW";
export type ProblemPriority = "P1" | "P2" | "P3" | "P4";
export type ActionStatus = "IN_PROGRESS" | "DONE";
export type LinkTargetType = "INCIDENT" | "CHANGE";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}

export interface ProblemSummary {
  id: number;
  ticketKey: string;
  summary: string;
  status: ProblemStatus;
  priority: ProblemPriority | null;
  origin: Origin;
  assignee?: string;
  updatedAt: string;
}

export interface Rca {
  rootCause: string;
  fiveWhys: string[];
  category: string;
}

export interface LinkedTicket {
  id: number;
  ticketKey: string;
}

export interface ProblemAction {
  id: number;
  description: string;
  status: ActionStatus;
}

export interface ProblemDetail {
  id: number;
  ticketKey: string;
  summary: string;
  description: string;
  status: ProblemStatus;
  priority: ProblemPriority | null;
  impact: Level | null;
  urgency: Level | null;
  origin?: Origin;
  investigationReason?: string;
  component?: string;
  rca: Rca | null;
  workaround: string | null;
  linkedIncidents: LinkedTicket[];
  linkedChanges: LinkedTicket[];
  actions: ProblemAction[];
  /** BE 제공 시 이 목록만 전이 버튼으로 노출. */
  allowedTransitions?: ProblemTargetStatus[];
}

export interface CreateProblemInput {
  summary: string;
  description?: string;
  origin?: Origin;
  investigationReason?: string;
  impact?: Level;
  urgency?: Level;
  component?: string;
}

export interface CreatedProblem {
  id: number;
  ticketKey: string;
  status: ProblemStatus;
  priority: ProblemPriority | null;
}

export interface ProblemListQuery {
  status?: ProblemStatus;
  priority?: ProblemPriority;
  origin?: Origin;
  assignee?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface WorkaroundInput {
  content: string;
  linkedArticleId?: number;
}

export interface KnownErrorInput {
  title: string;
  rootCause: string;
  workaround: string;
}

export interface CreatedKnownError {
  id: number;
  title: string;
}

export interface KnownError {
  id: number;
  title: string;
  rootCause: string;
  workaround: string;
  problemKey: string;
}

export interface LinkInput {
  targetType: LinkTargetType;
  targetId?: number;
  createNewChange?: boolean;
}

export interface ActionInput {
  description: string;
  owner?: string;
  dueDate?: string;
}

export interface CloseResult {
  id: number;
  status: ProblemStatus;
  warning: string | null;
}
