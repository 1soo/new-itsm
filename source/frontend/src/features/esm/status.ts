import type { TFunction } from "i18next";

import type { StatusTone } from "@/components/common";
import type {
  ChecklistStatus,
  ChecklistTaskStatus,
  ChecklistTemplateType,
  ChecklistType,
  Department,
  EsmRequestStatus,
  HrCaseStatus,
  HrCaseTargetStatus,
} from "@/features/esm/types";

/* ESM 부서·상태 표시 매핑 — common.md 2.1절 시맨틱 색상, screen/esm.md 팔레트 기준. */

/** 카탈로그 항목 담당 부서(ESM 신규 부서만, IT는 기존 SRM 유지). */
export const DEPARTMENTS: Department[] = ["HR", "LEGAL", "FACILITIES", "FINANCE"];

/** 체크리스트 템플릿 하위 작업 배정 부서(IT 포함 — 온보딩/오프보딩 시 IT 협조 작업이 있을 수 있음). */
export const TASK_DEPARTMENTS: Department[] = ["HR", "LEGAL", "FACILITIES", "FINANCE", "IT"];

const DEPARTMENT_LABEL: Record<Department, string> = {
  HR: "인사",
  LEGAL: "법무",
  FACILITIES: "시설",
  FINANCE: "재무",
  IT: "IT",
};

/** 부서 라벨(`esm:department.*`). */
export function departmentLabel(t: TFunction, department: Department | string | null | undefined): string {
  if (!department) return "";
  return t(`department.${department}`, { ns: "esm", defaultValue: DEPARTMENT_LABEL[department as Department] ?? department });
}

const REQUEST_STATUS_LABEL: Record<EsmRequestStatus, string> = {
  SUBMITTED: "제출됨",
  IN_PROGRESS: "처리중",
  COMPLETED: "완료",
  REJECTED: "반려",
};

const REQUEST_STATUS_TONE: Record<EsmRequestStatus, StatusTone> = {
  SUBMITTED: "info",
  IN_PROGRESS: "warning",
  COMPLETED: "success",
  REJECTED: "danger",
};

/** 부서 요청 상태 라벨(`esm:requestStatus.*`). */
export function requestStatusLabel(t: TFunction, status: EsmRequestStatus | null | undefined): string {
  if (!status) return "";
  return t(`requestStatus.${status}`, { ns: "esm", defaultValue: REQUEST_STATUS_LABEL[status] ?? status });
}

export function requestStatusTone(status: EsmRequestStatus): StatusTone {
  return REQUEST_STATUS_TONE[status] ?? "muted";
}

const HR_CASE_STATUS_LABEL: Record<HrCaseStatus, string> = {
  INTAKE: "접수",
  DOCUMENTATION: "기록",
  INVESTIGATION: "조사",
  RESOLUTION: "해결",
};

const HR_CASE_STATUS_TONE: Record<HrCaseStatus, StatusTone> = {
  INTAKE: "info",
  DOCUMENTATION: "warning",
  INVESTIGATION: "warning",
  RESOLUTION: "success",
};

/** HR 케이스 상태 라벨(`esm:hrCaseStatus.*`). */
export function hrCaseStatusLabel(t: TFunction, status: HrCaseStatus | string | null | undefined): string {
  if (!status) return "";
  return t(`hrCaseStatus.${status}`, { ns: "esm", defaultValue: HR_CASE_STATUS_LABEL[status as HrCaseStatus] ?? status });
}

export function hrCaseStatusTone(status: HrCaseStatus | string): StatusTone {
  return HR_CASE_STATUS_TONE[status as HrCaseStatus] ?? "muted";
}

/** HR 케이스 4단계 순차 전이 — 현재 상태에서 허용되는 다음 단계 하나만 반환(없으면 최종 단계). */
const HR_CASE_NEXT: Record<HrCaseStatus, HrCaseTargetStatus | null> = {
  INTAKE: "DOCUMENTATION",
  DOCUMENTATION: "INVESTIGATION",
  INVESTIGATION: "RESOLUTION",
  RESOLUTION: null,
};

export function hrCaseNextStatus(status: HrCaseStatus): HrCaseTargetStatus | null {
  return HR_CASE_NEXT[status] ?? null;
}

const CHECKLIST_STATUS_LABEL: Record<ChecklistStatus, string> = {
  IN_PROGRESS: "진행중",
  COMPLETED: "완료",
};

const CHECKLIST_STATUS_TONE: Record<ChecklistStatus, StatusTone> = {
  IN_PROGRESS: "warning",
  COMPLETED: "success",
};

/** 체크리스트 상태 라벨(`esm:checklistStatus.*`). */
export function checklistStatusLabel(t: TFunction, status: ChecklistStatus | null | undefined): string {
  if (!status) return "";
  return t(`checklistStatus.${status}`, { ns: "esm", defaultValue: CHECKLIST_STATUS_LABEL[status] ?? status });
}

export function checklistStatusTone(status: ChecklistStatus): StatusTone {
  return CHECKLIST_STATUS_TONE[status] ?? "muted";
}

const CHECKLIST_TASK_STATUS_LABEL: Record<ChecklistTaskStatus, string> = {
  PENDING: "대기",
  DONE: "완료",
};

const CHECKLIST_TASK_STATUS_TONE: Record<ChecklistTaskStatus, StatusTone> = {
  PENDING: "warning",
  DONE: "success",
};

/** 체크리스트 하위 작업 상태 라벨(`esm:checklistTaskStatus.*`). */
export function checklistTaskStatusLabel(t: TFunction, status: ChecklistTaskStatus | null | undefined): string {
  if (!status) return "";
  return t(`checklistTaskStatus.${status}`, { ns: "esm", defaultValue: CHECKLIST_TASK_STATUS_LABEL[status] ?? status });
}

export function checklistTaskStatusTone(status: ChecklistTaskStatus): StatusTone {
  return CHECKLIST_TASK_STATUS_TONE[status] ?? "muted";
}

const CHECKLIST_TYPE_LABEL: Record<ChecklistType, string> = {
  ONBOARDING: "온보딩",
  OFFBOARDING: "오프보딩",
};

/** 체크리스트 유형 라벨(`esm:checklistType.*`, ChecklistDetailPage·MyChecklistTasksPage 공용). */
export function checklistTypeLabel(t: TFunction, ty: ChecklistType | null | undefined): string {
  if (!ty) return "";
  return t(`checklistType.${ty}`, { ns: "esm", defaultValue: CHECKLIST_TYPE_LABEL[ty] ?? ty });
}

const CHECKLIST_TEMPLATE_TYPE_LABEL: Record<ChecklistTemplateType, string> = {
  NONE: "없음",
  ONBOARDING: "온보딩",
  OFFBOARDING: "오프보딩",
};

/** 카탈로그 체크리스트 템플릿 유형 라벨(`esm:checklistTemplateType.*`, EsmCatalogManagePage 전용). */
export function checklistTemplateTypeLabel(t: TFunction, ty: ChecklistTemplateType | null | undefined): string {
  if (!ty) return "";
  return t(`checklistTemplateType.${ty}`, { ns: "esm", defaultValue: CHECKLIST_TEMPLATE_TYPE_LABEL[ty] ?? ty });
}
