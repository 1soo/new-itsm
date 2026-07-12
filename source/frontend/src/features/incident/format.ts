import type { TFunction } from "i18next";

/* INC 화면 공통 표시 포맷터. */

export function formatDate(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleDateString("ko-KR");
}

export function formatDateTime(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString("ko-KR");
}

/** 분 단위 지표 표시(null이면 미산정 라벨, `incident:metrics.notCalculated`). 단위는 `incident:metrics.minutesUnit`(IncidentMetricsPage와 동일 키)로 통일. */
export function formatMinutes(t: TFunction, m: number | null | undefined): string {
  if (m == null) return t("metrics.notCalculated", { ns: "incident", defaultValue: "미산정" });
  const unit = t("metrics.minutesUnit", { ns: "incident", defaultValue: "분" });
  return `${Math.round(m)} ${unit}`;
}
