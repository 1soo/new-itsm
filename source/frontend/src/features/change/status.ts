import type { StatusTone } from "@/components/common";
import type {
  ChangeStatus,
  ChangeTargetStatus,
  ChangeType,
  Risk,
} from "@/features/change/types";

/* CHG 상태·유형·위험도 표시 매핑 — common.md 시맨틱 색상, change.md 팔레트. 승인 경로(구 CAB 자동 라우팅)는
   승인 프로세스 커스텀 기능(유지보수 요청)으로 완전 제거되어 별도 라벨/tone이 없다. */

const STATUS_LABEL: Record<ChangeStatus, string> = {
  REQUESTED: "요청",
  REVIEW: "검토",
  PLANNING: "계획",
  APPROVAL: "승인",
  IMPLEMENTATION: "구현",
  CLOSED: "종료",
};

const STATUS_TONE: Record<ChangeStatus, StatusTone> = {
  REQUESTED: "muted",
  REVIEW: "warning",
  PLANNING: "warning",
  APPROVAL: "warning",
  IMPLEMENTATION: "info",
  CLOSED: "success",
};

export function statusLabel(s: ChangeStatus): string {
  return STATUS_LABEL[s] ?? s;
}
export function statusTone(s: ChangeStatus): StatusTone {
  return STATUS_TONE[s] ?? "muted";
}

const TYPE_LABEL: Record<ChangeType, string> = {
  STANDARD: "표준",
  NORMAL: "일반",
  EMERGENCY: "긴급",
};

const TYPE_TONE: Record<ChangeType, StatusTone> = {
  STANDARD: "info",
  NORMAL: "muted",
  EMERGENCY: "warning",
};

export function typeLabel(t: ChangeType): string {
  return TYPE_LABEL[t] ?? t;
}
export function typeTone(t: ChangeType): StatusTone {
  return TYPE_TONE[t] ?? "muted";
}

const RISK_LABEL: Record<Risk, string> = {
  HIGH: "높음",
  MEDIUM: "보통",
  LOW: "낮음",
};

const RISK_TONE: Record<Risk, StatusTone> = {
  HIGH: "danger",
  MEDIUM: "warning",
  LOW: "muted",
};

export function riskLabel(r: Risk): string {
  return RISK_LABEL[r] ?? r;
}
export function riskTone(r: Risk): StatusTone {
  return RISK_TONE[r] ?? "muted";
}

export const CHANGE_TYPES: ChangeType[] = ["STANDARD", "NORMAL", "EMERGENCY"];
export const RISKS: Risk[] = ["HIGH", "MEDIUM", "LOW"];
export const CHANGE_STATUSES: ChangeStatus[] = [
  "REQUESTED",
  "REVIEW",
  "PLANNING",
  "APPROVAL",
  "IMPLEMENTATION",
  "CLOSED",
];

/** 6단계 순서 전이 — BE allowedTransitions 미제공 시 fallback(다음 단계 1개). */
const STATUS_SEQUENCE: ChangeStatus[] = CHANGE_STATUSES;

export function fallbackTransitions(status: ChangeStatus): ChangeTargetStatus[] {
  const idx = STATUS_SEQUENCE.indexOf(status);
  if (idx < 0 || idx >= STATUS_SEQUENCE.length - 1) return [];
  return [STATUS_SEQUENCE[idx + 1] as ChangeTargetStatus];
}
