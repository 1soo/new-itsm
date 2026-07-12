import type { TFunction } from "i18next";

import type { StatusTone } from "@/components/common";
import type { MetricType, ThresholdType } from "@/features/infra-monitoring/types";

/* IOM 지표 항목·임계치·활용률 표시 매핑 — common.md 시맨틱 색상, infra-monitoring.md 팔레트. */

export const METRIC_TYPES: MetricType[] = ["UPTIME", "CPU", "MEMORY", "RESPONSE_TIME"];

const METRIC_TYPE_LABEL: Record<MetricType, string> = {
  UPTIME: "가동률",
  CPU: "CPU",
  MEMORY: "메모리",
  RESPONSE_TIME: "응답시간",
};

/** 지표 항목 라벨(`infra-monitoring:metricType.*`). */
export function metricTypeLabel(t: TFunction, ty: string | null | undefined): string {
  if (!ty) return "";
  return t(`metricType.${ty}`, { ns: "infra-monitoring", defaultValue: METRIC_TYPE_LABEL[ty as MetricType] ?? ty });
}

const METRIC_TYPE_UNIT: Record<MetricType, string> = {
  UPTIME: "%",
  CPU: "%",
  MEMORY: "%",
  RESPONSE_TIME: "ms",
};

/** 지표 항목 단위(%, ms) — 기호이므로 번역하지 않는다. */
export function metricTypeUnit(ty: string): string {
  return METRIC_TYPE_UNIT[ty as MetricType] ?? "";
}

const THRESHOLD_TYPE_LABEL: Record<ThresholdType, string> = {
  UPPER: "상한 초과",
  LOWER: "하한 미달",
};

/** 임계치 초과 유형 라벨(`infra-monitoring:thresholdType.*`). */
export function thresholdTypeLabel(t: TFunction, ty: ThresholdType | null | undefined): string {
  if (!ty) return "";
  return t(`thresholdType.${ty}`, { ns: "infra-monitoring", defaultValue: THRESHOLD_TYPE_LABEL[ty] ?? ty });
}

/** 용량 활용률 배지 tone — 100% 초과 Danger, 80% 이상 Warning, 그 외 Success. */
export function utilizationTone(rate: number): StatusTone {
  if (rate > 100) return "danger";
  if (rate >= 80) return "warning";
  return "success";
}

/** SLA 대비 가동률 달성 여부 배지 tone — 목표 미설정(null)은 muted. */
export function slaMetTone(met: boolean | null): StatusTone {
  if (met == null) return "muted";
  return met ? "success" : "danger";
}
