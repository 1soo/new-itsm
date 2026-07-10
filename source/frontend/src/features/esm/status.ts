import type { StatusTone } from "@/components/common";
import type {
  ChecklistStatus,
  ChecklistTaskStatus,
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

export function departmentLabel(department: Department | string): string {
  return DEPARTMENT_LABEL[department as Department] ?? department;
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

export function requestStatusLabel(status: EsmRequestStatus): string {
  return REQUEST_STATUS_LABEL[status] ?? status;
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

export function hrCaseStatusLabel(status: HrCaseStatus | string): string {
  return HR_CASE_STATUS_LABEL[status as HrCaseStatus] ?? status;
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

export function checklistStatusLabel(status: ChecklistStatus): string {
  return CHECKLIST_STATUS_LABEL[status] ?? status;
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

export function checklistTaskStatusLabel(status: ChecklistTaskStatus): string {
  return CHECKLIST_TASK_STATUS_LABEL[status] ?? status;
}

export function checklistTaskStatusTone(status: ChecklistTaskStatus): StatusTone {
  return CHECKLIST_TASK_STATUS_TONE[status] ?? "muted";
}
