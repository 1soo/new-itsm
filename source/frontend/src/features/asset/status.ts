import type { TFunction } from "i18next";

import type { StatusTone } from "@/components/common";
import type {
  AssetStatus,
  AssetType,
  CiRelationType,
  ExpiryStatus,
  TicketType,
} from "@/features/asset/types";

/* ITAM 유형·상태·만료 표시 매핑 — common.md 시맨틱 색상, asset.md 팔레트. */

const TYPE_LABEL: Record<AssetType, string> = {
  HARDWARE: "하드웨어",
  SOFTWARE: "소프트웨어",
  CLOUD: "클라우드",
};

const TYPE_TONE: Record<AssetType, StatusTone> = {
  HARDWARE: "info",
  SOFTWARE: "muted",
  CLOUD: "info",
};

/** 자산 유형 라벨(`asset:type.*`). */
export function typeLabel(t: TFunction, ty: AssetType | null | undefined): string {
  if (!ty) return "";
  return t(`type.${ty}`, { ns: "asset", defaultValue: TYPE_LABEL[ty] ?? ty });
}
export function typeTone(t: AssetType): StatusTone {
  return TYPE_TONE[t] ?? "muted";
}

const STATUS_LABEL: Record<AssetStatus, string> = {
  PLANNING: "계획",
  PROCUREMENT: "구매",
  OPERATION: "운영",
  MAINTENANCE: "유지보수",
  RETIREMENT: "폐기",
};

const STATUS_TONE: Record<AssetStatus, StatusTone> = {
  PLANNING: "muted",
  PROCUREMENT: "muted",
  OPERATION: "success",
  MAINTENANCE: "warning",
  RETIREMENT: "danger",
};

/** 자산 생애주기 상태 라벨(`asset:status.*`). */
export function statusLabel(t: TFunction, s: AssetStatus | null | undefined): string {
  if (!s) return "";
  return t(`status.${s}`, { ns: "asset", defaultValue: STATUS_LABEL[s] ?? s });
}
export function statusTone(s: AssetStatus): StatusTone {
  return STATUS_TONE[s] ?? "muted";
}

const EXPIRY_LABEL: Record<ExpiryStatus, string> = {
  OK: "정상",
  EXPIRING: "임박",
  EXPIRED: "경과",
};

const EXPIRY_TONE: Record<ExpiryStatus, StatusTone> = {
  OK: "muted",
  EXPIRING: "warning",
  EXPIRED: "danger",
};

/** 만료 상태 라벨(`asset:expiry.*`). */
export function expiryLabel(t: TFunction, s: ExpiryStatus | null | undefined): string {
  if (!s) return "";
  return t(`expiry.${s}`, { ns: "asset", defaultValue: EXPIRY_LABEL[s] ?? s });
}
export function expiryTone(s: ExpiryStatus): StatusTone {
  return EXPIRY_TONE[s] ?? "muted";
}

const TICKET_TYPE_LABEL: Record<TicketType, string> = {
  SERVICE_REQUEST: "서비스 요청",
  INCIDENT: "인시던트",
  PROBLEM: "문제",
  CHANGE: "변경",
};

/** 연계 티켓 유형 라벨(`asset:ticketType.*`, AssetDetailPage 연계 폼·연결 티켓 나열 공용). */
export function ticketTypeLabel(t: TFunction, ty: TicketType | null | undefined): string {
  if (!ty) return "";
  return t(`ticketType.${ty}`, { ns: "asset", defaultValue: TICKET_TYPE_LABEL[ty] ?? ty });
}

const RELATION_TYPE_LABEL: Record<CiRelationType, string> = {
  DEPENDS_ON: "의존",
  RUNS_ON: "실행",
  CONNECTS_TO: "연결",
};

/** CI 관계 유형 라벨(`asset:relationType.*`, CiRelationPage 관계 추가 폼·영향 범위 나열 공용). */
export function relationTypeLabel(t: TFunction, ty: CiRelationType | null | undefined): string {
  if (!ty) return "";
  return t(`relationType.${ty}`, { ns: "asset", defaultValue: RELATION_TYPE_LABEL[ty] ?? ty });
}

export const ASSET_TYPES: AssetType[] = ["HARDWARE", "SOFTWARE", "CLOUD"];
export const ASSET_STATUSES: AssetStatus[] = ["PLANNING", "PROCUREMENT", "OPERATION", "MAINTENANCE", "RETIREMENT"];
