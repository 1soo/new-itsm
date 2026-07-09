/* service-request(SRM) 도메인 타입 — api_spec/service-request.md 계약 기준. */

// 동적 폼 스키마는 dev-ui 공통 컴포넌트(DynamicForm/FieldBuilder)와 계약을 공유한다.
export type { FormFieldSchema } from "@/components/common";
import type { FormFieldSchema } from "@/components/common";

export type SrStatus =
  | "SUBMITTED"
  | "VALIDATED"
  | "ROUTED"
  | "APPROVAL_PENDING"
  | "IN_FULFILLMENT"
  | "FULFILLED"
  | "CLOSED"
  | "REJECTED";

export type SlaStatus = "OK" | "WARNING" | "BREACHED";

export type TargetStatus = "VALIDATED" | "ROUTED" | "IN_FULFILLMENT" | "FULFILLED" | "CLOSED";

export interface Queue {
  id: number;
  name: string;
  isDefault: boolean;
  openCount: number;
}

export interface CatalogItemSummary {
  id: number;
  name: string;
  description?: string;
  category?: string;
  approvalRequired: boolean;
}

export interface CatalogItemDetail {
  id: number;
  name: string;
  description: string;
  approvalRequired: boolean;
  slaResponseMinutes: number;
  slaResolveMinutes: number;
  formSchema: FormFieldSchema[];
}

export interface CatalogItemInput {
  name: string;
  description: string;
  approvalRequired: boolean;
  queueId?: number;
  slaResponseMinutes: number;
  slaResolveMinutes: number;
  formSchema: FormFieldSchema[];
}

export interface KnowledgeSuggestion {
  articleId: number;
  title: string;
  score: number;
}

export interface CreateRequestInput {
  catalogItemId: number;
  formValues: Record<string, unknown>;
}

export interface CreatedRequest {
  id: number;
  ticketKey: string;
  status: SrStatus;
  createdAt: string;
}

export interface RequestSummary {
  id: number;
  ticketKey: string;
  catalogItemName: string;
  status: SrStatus;
  slaStatus: SlaStatus;
  assignee?: string;
  updatedAt: string;
}

export interface RequestComment {
  id: number;
  author: string;
  body: string;
  createdAt: string;
}

export interface RequestTimelineEvent {
  type: string;
  message: string;
  at: string;
}

export interface RequestApproval {
  required: boolean;
  status?: "PENDING" | "APPROVED" | "REJECTED";
  reason?: string;
}

export interface RequestSla {
  responseStatus: string;
  resolveStatus: string;
}

export interface LinkedArticle {
  articleId: number;
  title: string;
}

export interface LinkedAsset {
  id: number;
  assetKey: string;
}

export interface RequestDetail {
  id: number;
  ticketKey: string;
  catalogItemName: string;
  status: SrStatus;
  formValues: Record<string, unknown>;
  requester: string;
  assignee?: string;
  queue?: string;
  approval: RequestApproval;
  sla: RequestSla;
  linkedArticles: LinkedArticle[];
  linkedAssets: LinkedAsset[];
  comments: RequestComment[];
  timeline: RequestTimelineEvent[];
  /** BE가 제공하면 이 목록만 전이 버튼으로 노출(허용 전이). 없으면 FE가 status/approval로 유추. */
  allowedTransitions?: TargetStatus[];
}

export interface ApprovalItem {
  requestId: number;
  ticketKey: string;
  requester: string;
  requestedAt: string;
}

export interface RequestMetrics {
  csatAvg: number;
  avgResponseMinutes: number;
  avgResolveMinutes: number;
  slaComplianceRate: number;
}

export interface RequestListQuery {
  scope?: "mine" | "all";
  queue?: string;
  status?: SrStatus | "";
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}
