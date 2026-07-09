import type { StatusTone } from "@/components/common";
import type { AssetStatus, AssetType, ExpiryStatus } from "@/features/asset/types";

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

export function typeLabel(t: AssetType): string {
  return TYPE_LABEL[t] ?? t;
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

export function statusLabel(s: AssetStatus): string {
  return STATUS_LABEL[s] ?? s;
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

export function expiryLabel(s: ExpiryStatus): string {
  return EXPIRY_LABEL[s] ?? s;
}
export function expiryTone(s: ExpiryStatus): StatusTone {
  return EXPIRY_TONE[s] ?? "muted";
}

export const ASSET_TYPES: AssetType[] = ["HARDWARE", "SOFTWARE", "CLOUD"];
export const ASSET_STATUSES: AssetStatus[] = ["PLANNING", "PROCUREMENT", "OPERATION", "MAINTENANCE", "RETIREMENT"];
