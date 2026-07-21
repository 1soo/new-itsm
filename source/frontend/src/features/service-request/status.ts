import type { TFunction } from "i18next";

import type { StatusTone } from "@/components/common";
import type { SlaStatus, SrStatus } from "@/features/service-request/types";

/* SRM 상태·SLA 표시 매핑 — common.md 2.1절 시맨틱 색상 기준(FE에서 tone·라벨 주입). */

const STATUS_LABEL: Record<SrStatus, string> = {
  SUBMITTED: "제출됨",
  VALIDATED: "검증됨",
  ROUTED: "라우팅됨",
  IN_FULFILLMENT: "이행 중",
  FULFILLED: "이행 완료",
  CLOSED: "종료",
};

const STATUS_TONE: Record<SrStatus, StatusTone> = {
  SUBMITTED: "info",
  VALIDATED: "info",
  ROUTED: "info",
  IN_FULFILLMENT: "info",
  FULFILLED: "success",
  CLOSED: "muted",
};

/** 요청 상태 라벨(`service-request:status.*`, 6.3절 전환 패턴). */
export function statusLabel(t: TFunction, status: SrStatus): string {
  return t(`status.${status}`, {
    ns: "service-request",
    defaultValue: STATUS_LABEL[status] ?? status,
  });
}

const TRANSITION_LABEL: Partial<Record<SrStatus, string>> = {
  VALIDATED: "검증 완료",
  ROUTED: "라우팅 처리",
  IN_FULFILLMENT: "이행 시작",
  FULFILLED: "이행 완료 처리",
  CLOSED: "종료 처리",
};

/** 상태 전이 버튼 라벨(동작 동사형, `service-request:transition.*`, SCR-COM-008 아키텍처). */
export function transitionLabel(t: TFunction, target: SrStatus): string {
  return t(`transition.${target}`, {
    ns: "service-request",
    defaultValue: TRANSITION_LABEL[target] ?? STATUS_LABEL[target] ?? target,
  });
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

/** SLA 상태 라벨(`service-request:sla.*`). */
export function slaLabel(t: TFunction, sla: SlaStatus): string {
  return t(`sla.${sla}`, { ns: "service-request", defaultValue: SLA_LABEL[sla] ?? sla });
}

export function slaTone(sla: SlaStatus): StatusTone {
  return SLA_TONE[sla] ?? "muted";
}
