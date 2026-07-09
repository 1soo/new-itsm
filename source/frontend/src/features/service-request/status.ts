import type { StatusTone } from "@/components/common";
import type { SlaStatus, SrStatus } from "@/features/service-request/types";

/* SRM 상태·SLA 표시 매핑 — common.md 2.1절 시맨틱 색상 기준(FE에서 tone·라벨 주입). */

const STATUS_LABEL: Record<SrStatus, string> = {
  SUBMITTED: "제출됨",
  VALIDATED: "검증됨",
  ROUTED: "라우팅됨",
  APPROVAL_PENDING: "승인 대기",
  IN_FULFILLMENT: "이행 중",
  FULFILLED: "이행 완료",
  CLOSED: "종료",
  REJECTED: "반려",
};

const STATUS_TONE: Record<SrStatus, StatusTone> = {
  SUBMITTED: "info",
  VALIDATED: "info",
  ROUTED: "info",
  APPROVAL_PENDING: "warning",
  IN_FULFILLMENT: "info",
  FULFILLED: "success",
  CLOSED: "muted",
  REJECTED: "danger",
};

export function statusLabel(status: SrStatus): string {
  return STATUS_LABEL[status] ?? status;
}

export function statusTone(status: SrStatus): StatusTone {
  return STATUS_TONE[status] ?? "muted";
}

const SLA_LABEL: Record<SlaStatus, string> = {
  OK: "준수",
  WARNING: "임박",
  BREACHED: "위반",
};

const SLA_TONE: Record<SlaStatus, StatusTone> = {
  OK: "success",
  WARNING: "warning",
  BREACHED: "danger",
};

export function slaLabel(sla: SlaStatus): string {
  return SLA_LABEL[sla] ?? sla;
}

export function slaTone(sla: SlaStatus): StatusTone {
  return SLA_TONE[sla] ?? "muted";
}
