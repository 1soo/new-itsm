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

export function metricTypeLabel(t: string): string {
  return METRIC_TYPE_LABEL[t as MetricType] ?? t;
}

const METRIC_TYPE_UNIT: Record<MetricType, string> = {
  UPTIME: "%",
  CPU: "%",
  MEMORY: "%",
  RESPONSE_TIME: "ms",
};

export function metricTypeUnit(t: string): string {
  return METRIC_TYPE_UNIT[t as MetricType] ?? "";
}

export function thresholdTypeLabel(t: ThresholdType): string {
  return t === "UPPER" ? "상한 초과" : "하한 미달";
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
