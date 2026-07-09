import { apiClient } from "@/lib/apiClient";
import type {
  CreateIncidentInput,
  CreatedIncident,
  EscalationType,
  IncidentDetail,
  IncidentListQuery,
  IncidentMetrics,
  IncidentSummary,
  IncidentTargetStatus,
  PageResponse,
  Postmortem,
  Priority,
  ResolveInput,
  ResponderRole,
  Severity,
  Visibility,
} from "@/features/incident/types";

/* INC API 호출 — 모두 공통 apiClient 경유. api_spec/incident.md 준수. */

function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") params[key] = value;
  }
  return params;
}

export const incidentApi = {
  // API-INC-001 목록
  async list(query: IncidentListQuery): Promise<PageResponse<IncidentSummary>> {
    const res = await apiClient.get<PageResponse<IncidentSummary>>("/incidents", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-INC-002 등록
  async create(body: CreateIncidentInput): Promise<CreatedIncident> {
    const res = await apiClient.post<CreatedIncident>("/incidents", body);
    return res.data;
  },

  // API-INC-003 상세
  async get(id: number): Promise<IncidentDetail> {
    const res = await apiClient.get<IncidentDetail>(`/incidents/${id}`);
    return res.data;
  },

  // API-INC-004 심각도·우선순위 변경
  async updateSeverity(id: number, severity: Severity, priority: Priority): Promise<void> {
    await apiClient.patch(`/incidents/${id}/severity`, { severity, priority });
  },

  // API-INC-005 상태 전이
  async transition(id: number, targetStatus: IncidentTargetStatus, note?: string): Promise<{ id: number; status: string }> {
    const res = await apiClient.patch<{ id: number; status: string }>(
      `/incidents/${id}/status`,
      { targetStatus, note },
    );
    return res.data;
  },

  // API-INC-006 대응 역할 배정 (Incident Manager)
  async assignRole(id: number, userId: number, role: ResponderRole): Promise<void> {
    await apiClient.post(`/incidents/${id}/roles`, { userId, role });
  },

  // API-INC-007 에스컬레이션
  async escalate(id: number, targetUserId: number, type: EscalationType, reason?: string): Promise<void> {
    await apiClient.post(`/incidents/${id}/escalate`, { targetUserId, type, reason });
  },

  // API-INC-008 상태 업데이트(타임라인)
  async addUpdate(id: number, message: string, visibility: Visibility): Promise<{ id: number; at: string }> {
    const res = await apiClient.post<{ id: number; at: string }>(`/incidents/${id}/updates`, {
      message,
      visibility,
    });
    return res.data;
  },

  // API-INC-009 해결 처리·시간 지표
  async resolve(id: number, body: ResolveInput): Promise<IncidentDetail> {
    const res = await apiClient.post<IncidentDetail>(`/incidents/${id}/resolve`, body);
    return res.data;
  },

  // API-INC-010 포스트모템 조회 (미작성 시 404)
  async getPostmortem(id: number): Promise<Postmortem> {
    const res = await apiClient.get<Postmortem>(`/incidents/${id}/postmortem`);
    return res.data;
  },

  // API-INC-011 포스트모템 작성/수정
  async savePostmortem(id: number, body: Postmortem): Promise<Postmortem> {
    const res = await apiClient.put<Postmortem>(`/incidents/${id}/postmortem`, body);
    return res.data;
  },

  // API-INC-012 문제 연계 (problem 도메인 도입 후 완성 — 현재 스텁)
  async linkProblem(id: number, problemId?: number, createNewProblem = false): Promise<{ incidentId: number; problemId: number }> {
    const res = await apiClient.post<{ incidentId: number; problemId: number }>(
      `/incidents/${id}/links`,
      { problemId, createNewProblem },
    );
    return res.data;
  },

  // API-INC-013 지표
  async metrics(params: { from?: string; to?: string } = {}): Promise<IncidentMetrics> {
    const res = await apiClient.get<IncidentMetrics>("/incidents/metrics", {
      params: cleanParams(params),
    });
    return res.data;
  },
};
