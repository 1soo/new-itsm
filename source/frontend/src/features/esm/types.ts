/* esm(엔터프라이즈 서비스 관리) 도메인 타입 — api_spec/esm.md 계약 기준. */

// 동적 폼 스키마는 공통 컴포넌트(DynamicFormBuilder/DynamicFormRenderer)와 계약을 공유한다
// (2026-07-19 유지보수 요청, 레거시 EAV → SRM과 공용인 자체 8×n 그리드 스키마로 전환).
export type { GridFormSchema } from "@/components/common";
import type { GridFormSchema } from "@/components/common";

export type Department = "HR" | "LEGAL" | "FACILITIES" | "FINANCE" | "IT";

export type ChecklistTemplateType = "NONE" | "ONBOARDING" | "OFFBOARDING";

export interface ChecklistTemplateTask {
  department: Department | string;
  taskDescription: string;
}

export interface CatalogItemSummary {
  id: number;
  name: string;
  description?: string;
  department: Department;
  checklistTemplateType: ChecklistTemplateType;
}

export interface CatalogItemDetail {
  id: number;
  name: string;
  description: string;
  department: Department;
  checklistTemplateType: ChecklistTemplateType;
  checklistTemplate: ChecklistTemplateTask[];
  formSchema: GridFormSchema;
}

export interface CatalogItemInput {
  name: string;
  description: string;
  department: Department;
  checklistTemplateType: ChecklistTemplateType;
  checklistTemplate: ChecklistTemplateTask[];
  formSchema: GridFormSchema;
}

export type EsmRequestStatus = "SUBMITTED" | "IN_PROGRESS" | "COMPLETED" | "REJECTED";
export type EsmRequestTargetStatus = "IN_PROGRESS" | "COMPLETED" | "REJECTED";

export interface CreateEsmRequestInput {
  catalogItemId: number;
  formValues: Record<string, unknown>;
  targetUserName?: string;
}

export interface CreatedEsmRequest {
  id: number;
  ticketKey: string;
  status: EsmRequestStatus;
  checklistId: number | null;
}

export interface EsmRequestSummary {
  id: number;
  ticketKey: string;
  catalogItemName: string;
  department: Department;
  status: EsmRequestStatus;
  updatedAt: string;
  /** 진행 중인 승인 인스턴스의 targetState(원본 코드값, 없으면 null). 2026-07-22 유지보수 요청 신규. */
  pendingApprovalTargetState: EsmRequestStatus | null;
}

export interface EsmRequestListQuery {
  scope?: "mine" | "all";
  department?: Department | "";
  status?: EsmRequestStatus | "";
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface EsmComment {
  id: number;
  author: string;
  body: string;
  createdAt: string;
}

export interface EsmTimelineEvent {
  type: string;
  message: string;
  at: string;
  actor: string;
}

/** 승인 프로세스 커스텀 기능(유지보수 요청) — approvalRequestId=null이면 매칭되는 승인 프로세스가 없어 게이트 없이 진행. */
export interface EsmApproval {
  approvalRequestId: number | null;
  status: "IN_PROGRESS" | "APPROVED" | "REJECTED" | null;
  /** 원본 코드값(도착 상태, 생성 시점 스냅샷). 2026-07-22 유지보수 요청 신규. */
  targetState: EsmRequestStatus | null;
}

export interface EsmRequestDetail {
  id: number;
  ticketKey: string;
  catalogItemName: string;
  department: Department;
  status: EsmRequestStatus;
  formValues: Record<string, unknown>;
  requester: string;
  assignee?: string;
  approval: EsmApproval;
  checklistId: number | null;
  comments: EsmComment[];
  timeline: EsmTimelineEvent[];
}

export type HrCaseStatus = "INTAKE" | "DOCUMENTATION" | "INVESTIGATION" | "RESOLUTION";
export type HrCaseTargetStatus = "DOCUMENTATION" | "INVESTIGATION" | "RESOLUTION";

export interface CreateHrCaseInput {
  subjectUserName: string;
  title: string;
  description?: string;
}

export interface CreatedHrCase {
  id: number;
  status: HrCaseStatus;
}

export interface HrCaseSummary {
  id: number;
  title: string;
  status: HrCaseStatus;
  updatedAt: string;
}

export interface HrCaseHistoryEntry {
  status: string;
  changedBy: string;
  at: string;
}

export interface HrCaseDetail {
  id: number;
  title: string;
  description: string;
  subjectUserName: string;
  status: HrCaseStatus;
  history: HrCaseHistoryEntry[];
}

export type ChecklistType = "ONBOARDING" | "OFFBOARDING";
export type ChecklistStatus = "IN_PROGRESS" | "COMPLETED";
export type ChecklistTaskStatus = "PENDING" | "DONE";

export interface ChecklistTask {
  id: number;
  department: Department | string;
  description: string;
  status: ChecklistTaskStatus;
  relatedAssetId: number | null;
  relatedAssetKey: string | null;
}

export interface ChecklistDetail {
  id: number;
  type: ChecklistType;
  targetUserName: string;
  status: ChecklistStatus;
  tasks: ChecklistTask[];
}

export interface MyChecklistTask {
  id: number;
  checklistId: number;
  checklistType: ChecklistType;
  targetUserName: string;
  description: string;
  status: ChecklistTaskStatus;
}

export interface EsmMetrics {
  requestCount: number;
  avgProcessingMinutes: number;
  onboardingCompletionRate: number;
  offboardingCompletionRate: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}
