import { apiClient } from "@/lib/apiClient";
import type {
  ComplianceAuditLog,
  ComplianceAuditLogQuery,
  ComplianceMetrics,
  CorrectiveActionInput,
  CorrectiveActionTargetStatus,
  CreatedCorrectiveAction,
  CreatedRequirement,
  CreateRequirementInput,
  PageResponse,
  RequirementDetail,
  RequirementListQuery,
  RequirementSummary,
  UpdateRequirementInput,
} from "@/features/compliance/types";

/* COMP API 호출 — 모두 공통 apiClient 경유. api_spec/compliance.md 준수. */

function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") params[key] = value;
  }
  return params;
}

export const complianceApi = {
  // API-COMP-001 목록
  async list(query: RequirementListQuery): Promise<PageResponse<RequirementSummary>> {
    const res = await apiClient.get<PageResponse<RequirementSummary>>("/compliance/requirements", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-COMP-002 등록
  async create(body: CreateRequirementInput): Promise<CreatedRequirement> {
    const res = await apiClient.post<CreatedRequirement>("/compliance/requirements", body);
    return res.data;
  },

  // API-COMP-003 상세
  async get(id: number): Promise<RequirementDetail> {
    const res = await apiClient.get<RequirementDetail>(`/compliance/requirements/${id}`);
    return res.data;
  },

  // API-COMP-004 요구사항 수정
  async update(id: number, body: UpdateRequirementInput): Promise<void> {
    await apiClient.patch(`/compliance/requirements/${id}`, body);
  },

  // API-COMP-005 변경 요청 연계
  async link(id: number, changeId: number): Promise<void> {
    await apiClient.post(`/compliance/requirements/${id}/links`, { changeId });
  },

  // API-COMP-006 책임자 지정
  async setOwner(id: number, ownerId: number): Promise<{ id: number; owner: string }> {
    const res = await apiClient.post<{ id: number; owner: string }>(`/compliance/requirements/${id}/owner`, {
      ownerId,
    });
    return res.data;
  },

  // API-COMP-007 시정조치 등록
  async addCorrectiveAction(id: number, body: CorrectiveActionInput): Promise<CreatedCorrectiveAction> {
    const res = await apiClient.post<CreatedCorrectiveAction>(
      `/compliance/requirements/${id}/corrective-actions`,
      body,
    );
    return res.data;
  },

  // API-COMP-008 시정조치 상태 전이
  async transitionAction(actionId: number, targetStatus: CorrectiveActionTargetStatus): Promise<void> {
    await apiClient.patch(`/compliance/corrective-actions/${actionId}/status`, { targetStatus });
  },

  // API-COMP-009 컴플라이언스 감사 로그 조회
  async auditLogs(query: ComplianceAuditLogQuery): Promise<ComplianceAuditLog[]> {
    const res = await apiClient.get<ComplianceAuditLog[]>("/compliance/audit-logs", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-COMP-010 준수 현황 조회
  async metrics(params: { from?: string; to?: string } = {}): Promise<ComplianceMetrics> {
    const res = await apiClient.get<ComplianceMetrics>("/compliance/metrics", {
      params: cleanParams(params),
    });
    return res.data;
  },
};
