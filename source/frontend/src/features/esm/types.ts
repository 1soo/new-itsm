/* esm(엔터프라이즈 서비스 관리) 도메인 타입 — api_spec/esm.md 계약 기준. */

// 동적 폼 스키마는 dev-ui 공통 컴포넌트(DynamicForm/FieldBuilder)와 계약을 공유한다.
export type { FormFieldSchema } from "@/components/common";
import type { FormFieldSchema } from "@/components/common";

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
  formSchema: FormFieldSchema[];
}

export interface CatalogItemInput {
  name: string;
  description: string;
  department: Department;
  checklistTemplateType: ChecklistTemplateType;
  checklistTemplate: ChecklistTemplateTask[];
  formSchema: FormFieldSchema[];
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
