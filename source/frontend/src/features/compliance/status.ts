import type { StatusTone } from "@/components/common";
import type { ComplianceStatus, CorrectiveActionStatus, CorrectiveActionTargetStatus } from "@/features/compliance/types";

/* COMP 준수 상태·시정조치 상태 표시 매핑 — common.md 시맨틱 색상, compliance.md 팔레트. */

const COMPLIANCE_STATUS_LABEL: Record<ComplianceStatus, string> = {
  COMPLIANT: "준수",
  NON_COMPLIANT: "미준수",
};

const COMPLIANCE_STATUS_TONE: Record<ComplianceStatus, StatusTone> = {
  COMPLIANT: "success",
  NON_COMPLIANT: "danger",
};

export function complianceStatusLabel(s: ComplianceStatus): string {
  return COMPLIANCE_STATUS_LABEL[s] ?? s;
}
export function complianceStatusTone(s: ComplianceStatus): StatusTone {
  return COMPLIANCE_STATUS_TONE[s] ?? "muted";
}

const ACTION_STATUS_LABEL: Record<CorrectiveActionStatus, string> = {
  DETECTED: "탐지",
  IN_PROGRESS: "조치중",
  RESOLVED: "해결",
};

const ACTION_STATUS_TONE: Record<CorrectiveActionStatus, StatusTone> = {
  DETECTED: "danger",
  IN_PROGRESS: "warning",
  RESOLVED: "success",
};

export function actionStatusLabel(s: CorrectiveActionStatus): string {
  return ACTION_STATUS_LABEL[s] ?? s;
}
export function actionStatusTone(s: CorrectiveActionStatus): StatusTone {
  return ACTION_STATUS_TONE[s] ?? "muted";
}

export const COMPLIANCE_STATUSES: ComplianceStatus[] = ["COMPLIANT", "NON_COMPLIANT"];

/** 시정조치 순서 전이(탐지→조치중→해결) — 다음 단계 1개만 허용. */
const ACTION_SEQUENCE: CorrectiveActionStatus[] = ["DETECTED", "IN_PROGRESS", "RESOLVED"];

export function nextActionTransition(status: CorrectiveActionStatus): CorrectiveActionTargetStatus | null {
  const idx = ACTION_SEQUENCE.indexOf(status);
  if (idx < 0 || idx >= ACTION_SEQUENCE.length - 1) return null;
  return ACTION_SEQUENCE[idx + 1] as CorrectiveActionTargetStatus;
}
