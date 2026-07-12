import type { TFunction } from "i18next";

import type { StatusTone } from "@/components/common";
import type {
  ActionStatus,
  Level,
  Origin,
  ProblemPriority,
  ProblemStatus,
  ProblemTargetStatus,
} from "@/features/problem/types";

/* PRB 상태·우선순위·출처 표시 매핑 — common.md 시맨틱 색상, problem.md 팔레트. */

const STATUS_LABEL: Record<ProblemStatus, string> = {
  DETECTION: "탐지",
  CLASSIFICATION: "분류",
  INVESTIGATION: "조사중",
  KNOWN_ERROR: "알려진 오류",
  WORKAROUND: "워크어라운드",
  RESOLVED_CLOSED: "종료",
};

const STATUS_TONE: Record<ProblemStatus, StatusTone> = {
  DETECTION: "danger",
  CLASSIFICATION: "warning",
  INVESTIGATION: "warning",
  KNOWN_ERROR: "info",
  WORKAROUND: "info",
  RESOLVED_CLOSED: "success",
};

/** 문제 상태 라벨(`problem:status.*`, 6.3절 전환 패턴). */
export function statusLabel(t: TFunction, s: ProblemStatus): string {
  return t(`status.${s}`, { ns: "problem", defaultValue: STATUS_LABEL[s] ?? s });
}
export function statusTone(s: ProblemStatus): StatusTone {
  return STATUS_TONE[s] ?? "muted";
}

const PRIORITY_TONE: Record<ProblemPriority, StatusTone> = {
  P1: "danger",
  P2: "warning",
  P3: "info",
  P4: "muted",
};

export function priorityTone(p: ProblemPriority): StatusTone {
  return PRIORITY_TONE[p] ?? "muted";
}

const ORIGIN_LABEL: Record<Origin, string> = {
  REACTIVE: "반응",
  PROACTIVE: "선제",
};

/** 문제 출처 라벨(`problem:origin.*`). 값이 없으면(레거시 데이터) 빈 문자열(기존 동작 유지). */
export function originLabel(t: TFunction, o: Origin | null | undefined): string {
  if (!o) return "";
  return t(`origin.${o}`, { ns: "problem", defaultValue: ORIGIN_LABEL[o] ?? o });
}

const LEVEL_LABEL: Record<Level, string> = {
  HIGH: "높음",
  MEDIUM: "보통",
  LOW: "낮음",
};

/** 영향도/긴급도 등급 라벨(`problem:level.*`). */
export function levelLabel(t: TFunction, l: Level): string {
  return t(`level.${l}`, { ns: "problem", defaultValue: LEVEL_LABEL[l] ?? l });
}

const ACTION_STATUS_LABEL: Record<ActionStatus, string> = {
  IN_PROGRESS: "진행중",
  DONE: "완료",
};

/** 후속 조치 상태 라벨(`problem:actionStatus.*`). */
export function actionStatusLabel(t: TFunction, s: ActionStatus): string {
  return t(`actionStatus.${s}`, { ns: "problem", defaultValue: ACTION_STATUS_LABEL[s] ?? s });
}

/*
 * 우선순위 매트릭스(영향도×긴급도) — 등록 화면 실시간 미리보기용.
 * BE 산정과 일치해야 한다(api_spec/problem.md, dev-backend 확정). 둘 중 하나라도 없으면 null=미산정.
 */
const PRIORITY_MATRIX: Record<Level, Record<Level, ProblemPriority>> = {
  HIGH: { HIGH: "P1", MEDIUM: "P2", LOW: "P3" },
  MEDIUM: { HIGH: "P2", MEDIUM: "P3", LOW: "P4" },
  LOW: { HIGH: "P3", MEDIUM: "P4", LOW: "P4" },
};

export function computePriority(
  impact: Level | "" | undefined,
  urgency: Level | "" | undefined,
): ProblemPriority | null {
  if (!impact || !urgency) return null;
  return PRIORITY_MATRIX[impact][urgency];
}

export const ORIGINS: Origin[] = ["REACTIVE", "PROACTIVE"];
export const LEVELS: Level[] = ["HIGH", "MEDIUM", "LOW"];
export const PROBLEM_PRIORITIES: ProblemPriority[] = ["P1", "P2", "P3", "P4"];
export const PROBLEM_STATUSES: ProblemStatus[] = [
  "DETECTION",
  "CLASSIFICATION",
  "INVESTIGATION",
  "KNOWN_ERROR",
  "WORKAROUND",
  "RESOLVED_CLOSED",
];

/** 6단계 순서 전이 — BE allowedTransitions 미제공 시 fallback(다음 단계 1개). */
const STATUS_SEQUENCE: ProblemStatus[] = PROBLEM_STATUSES;

export function fallbackTransitions(status: ProblemStatus): ProblemTargetStatus[] {
  const idx = STATUS_SEQUENCE.indexOf(status);
  if (idx < 0 || idx >= STATUS_SEQUENCE.length - 1) return [];
  return [STATUS_SEQUENCE[idx + 1] as ProblemTargetStatus];
}
