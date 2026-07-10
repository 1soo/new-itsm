/* infra-monitoring(IOM) 도메인 타입 — api_spec/infra-monitoring.md 계약 기준. */

export type MetricType = "UPTIME" | "CPU" | "MEMORY" | "RESPONSE_TIME";
export type ThresholdType = "UPPER" | "LOWER";

export interface CreateMetricInput {
  assetId: number;
  metricType: MetricType;
  value: number;
  measuredAt?: string;
}

export interface CreatedMetric {
  id: number;
  alertGenerated: boolean;
}

export interface MetricPoint {
  id: number;
  metricType: string;
  value: number;
  measuredAt: string;
}

export interface MetricQuery {
  assetId?: number;
  metricType?: MetricType;
  from?: string;
  to?: string;
}

export interface MetricThreshold {
  metricType: string;
  upperLimit: number | null;
  lowerLimit: number | null;
}

export interface ThresholdInput {
  upperLimit: number | null;
  lowerLimit: number | null;
}

export interface MetricAlert {
  id: number;
  assetKey: string;
  metricType: string;
  value: number;
  thresholdType: ThresholdType;
  acknowledged: boolean;
  occurredAt: string;
}

export interface AlertQuery {
  assetId?: number;
  acknowledged?: boolean;
}

export interface UptimeTargetInput {
  targetPercentage: number;
}

export interface UptimeStatus {
  assetKey: string;
  targetPercentage: number | null;
  actualPercentage: number | null;
  met: boolean | null;
}

export interface CapacityPlanInput {
  teamOrService: string;
  capacity: number;
  demand: number;
}

export interface CreatedCapacityPlan {
  id: number;
}

export interface CapacityPlan {
  id: number;
  teamOrService: string;
  capacity: number;
  demand: number;
  utilizationRate: number;
}

export interface ReportQuery {
  from?: string;
  to?: string;
  assetId?: number;
}

export interface InfraReport {
  avgUptime: number;
  avgCpu: number;
  avgMemory: number;
  avgResponseTime: number;
  avgCapacityUtilization: number;
}
