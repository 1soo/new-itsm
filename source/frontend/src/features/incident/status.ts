import type { TFunction } from "i18next";

import type { StatusTone } from "@/components/common";
import type { IncidentStatus, Severity } from "@/features/incident/types";

/* INC 상태·심각도 표시 매핑 — common.md 2.1절 시맨틱 색상, incident.md 팔레트. */

const STATUS_LABEL: Record<IncidentStatus, string> = {
  NEW: "신규",
  IN_PROGRESS: "대응중",
  RESOLVED: "해결",
  CLOSED: "종료",
};

const STATUS_TONE: Record<IncidentStatus, StatusTone> = {
  NEW: "danger",
  IN_PROGRESS: "info",
  RESOLVED: "success",
  CLOSED: "muted",
};

/** 인시던트 상태 라벨(`incident:status.*`, 6.3절 전환 패턴). */
export function statusLabel(t: TFunction, s: IncidentStatus): string {
  return t(`status.${s}`, { ns: "incident", defaultValue: STATUS_LABEL[s] ?? s });
}

export function statusTone(s: IncidentStatus): StatusTone {
  return STATUS_TONE[s] ?? "muted";
}

const SEVERITY_TONE: Record<Severity, StatusTone> = {
  SEV1: "danger",
  SEV2: "warning",
  SEV3: "info",
};

export function severityTone(s: Severity): StatusTone {
  return SEVERITY_TONE[s] ?? "muted";
}

export const SEVERITIES: Severity[] = ["SEV1", "SEV2", "SEV3"];
export const PRIORITIES = ["P1", "P2", "P3", "P4"] as const;
export const INCIDENT_STATUSES: IncidentStatus[] = ["NEW", "IN_PROGRESS", "RESOLVED", "CLOSED"];
