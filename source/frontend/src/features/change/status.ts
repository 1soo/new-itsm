import type { TFunction } from "i18next";

import type { StatusTone } from "@/components/common";
import type {
  ChangeStatus,
  ChangeTargetStatus,
  ChangeType,
  LinkedItemType,
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

/** 변경 상태 라벨(`change:status.*`, 6.3절 전환 패턴). */
export function statusLabel(t: TFunction, s: ChangeStatus | null | undefined): string {
  if (!s) return "";
  return t(`status.${s}`, { ns: "change", defaultValue: STATUS_LABEL[s] ?? s });
}
export function statusTone(s: ChangeStatus): StatusTone {
  return STATUS_TONE[s] ?? "muted";
}

const TRANSITION_LABEL: Partial<Record<ChangeStatus, string>> = {
  REVIEW: "검토 시작",
  PLANNING: "계획 수립",
  APPROVAL: "승인 요청",
  IMPLEMENTATION: "구현 시작",
  CLOSED: "종료 처리",
};

/** 상태 전이 버튼 라벨(동작 동사형, `change:transition.*`, SCR-COM-008 아키텍처). */
export function transitionLabel(t: TFunction, target: ChangeStatus): string {
  return t(`transition.${target}`, {
    ns: "change",
    defaultValue: TRANSITION_LABEL[target] ?? STATUS_LABEL[target] ?? target,
  });
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

/** 변경 유형 라벨(`change:type.*`). */
export function typeLabel(t: TFunction, ty: ChangeType | null | undefined): string {
  if (!ty) return "";
  return t(`type.${ty}`, { ns: "change", defaultValue: TYPE_LABEL[ty] ?? ty });
}
export function typeTone(ty: ChangeType): StatusTone {
  return TYPE_TONE[ty] ?? "muted";
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

/** 위험도 라벨(`change:risk.*`). */
export function riskLabel(t: TFunction, r: Risk | null | undefined): string {
  if (!r) return "";
  return t(`risk.${r}`, { ns: "change", defaultValue: RISK_LABEL[r] ?? r });
}
export function riskTone(r: Risk): StatusTone {
  return RISK_TONE[r] ?? "muted";
}

const LINK_TARGET_LABEL: Record<LinkedItemType, string> = {
  INCIDENT: "인시던트",
  PROBLEM: "문제",
  ASSET: "자산",
  COMPLIANCE_REQUIREMENT: "컴플라이언스 요구사항",
};

const LINK_TARGET_KEY: Record<LinkedItemType, string> = {
  INCIDENT: "changeDetail.linkTargetIncident",
  PROBLEM: "changeDetail.linkTargetProblem",
  ASSET: "changeDetail.linkTargetAsset",
  COMPLIANCE_REQUIREMENT: "changeDetail.linkTargetComplianceRequirement",
};

/** 연계 항목 유형 라벨(`change:changeDetail.linkTarget*`, ChangeDetailPage 연계 대상 드롭다운·나열 패널 공용). */
export function linkTargetLabel(t: TFunction, ty: LinkedItemType | null | undefined): string {
  if (!ty) return "";
  return t(LINK_TARGET_KEY[ty], { defaultValue: LINK_TARGET_LABEL[ty] ?? ty });
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
