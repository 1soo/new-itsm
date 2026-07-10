import { apiClient } from "@/lib/apiClient";
import type {
  AlertQuery,
  CapacityPlan,
  CapacityPlanInput,
  CreatedCapacityPlan,
  CreatedMetric,
  CreateMetricInput,
  InfraReport,
  MetricAlert,
  MetricPoint,
  MetricQuery,
  MetricThreshold,
  MetricType,
  ReportQuery,
  ThresholdInput,
  UptimeStatus,
  UptimeTargetInput,
} from "@/features/infra-monitoring/types";

/* IOM API 호출 — 모두 공통 apiClient 경유. api_spec/infra-monitoring.md 준수. */

function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") params[key] = value;
  }
  return params;
}

export const infraApi = {
  // API-IOM-001 지표 등록
  async registerMetric(body: CreateMetricInput): Promise<CreatedMetric> {
    const res = await apiClient.post<CreatedMetric>("/infra/metrics", body);
    return res.data;
  },

  // API-IOM-002 지표 시계열 조회
  async listMetrics(query: MetricQuery): Promise<MetricPoint[]> {
    const res = await apiClient.get<MetricPoint[]>("/infra/metrics", { params: cleanParams(query) });
    return res.data;
  },

  // API-IOM-003 임계치 목록 조회
  async listThresholds(): Promise<MetricThreshold[]> {
    const res = await apiClient.get<MetricThreshold[]>("/infra/metric-thresholds");
    return res.data;
  },

  // API-IOM-004 임계치 설정
  async setThreshold(metricType: MetricType, body: ThresholdInput): Promise<void> {
    await apiClient.put(`/infra/metric-thresholds/${metricType}`, body);
  },

  // API-IOM-005 임계치 초과 알림 목록 조회
  async listAlerts(query: AlertQuery): Promise<MetricAlert[]> {
    const res = await apiClient.get<MetricAlert[]>("/infra/metric-alerts", { params: cleanParams(query) });
    return res.data;
  },

  // API-IOM-006 임계치 초과 알림 확인 처리
  async acknowledgeAlert(id: number): Promise<void> {
    await apiClient.patch(`/infra/metric-alerts/${id}/acknowledge`);
  },

  // API-IOM-007 자산 가동률 목표 설정
  async setUptimeTarget(assetId: number, body: UptimeTargetInput): Promise<void> {
    await apiClient.put(`/infra/assets/${assetId}/uptime-target`, body);
  },

  // API-IOM-008 자산 가동률 현황 조회
  async getUptimeStatus(assetId: number, query: { from?: string; to?: string } = {}): Promise<UptimeStatus> {
    const res = await apiClient.get<UptimeStatus>(`/infra/assets/${assetId}/uptime`, { params: cleanParams(query) });
    return res.data;
  },

  // API-IOM-009 용량 계획 등록
  async createCapacityPlan(body: CapacityPlanInput): Promise<CreatedCapacityPlan> {
    const res = await apiClient.post<CreatedCapacityPlan>("/infra/capacity-plans", body);
    return res.data;
  },

  // API-IOM-010 용량 계획 목록 조회
  async listCapacityPlans(): Promise<CapacityPlan[]> {
    const res = await apiClient.get<CapacityPlan[]>("/infra/capacity-plans");
    return res.data;
  },

  // API-IOM-011 인프라 지표 리포팅 조회
  async getReport(query: ReportQuery = {}): Promise<InfraReport> {
    const res = await apiClient.get<InfraReport>("/infra/metrics/report", { params: cleanParams(query) });
    return res.data;
  },
};
