import { apiClient } from "@/lib/apiClient";
import type {
  ChangeDetail,
  ChangeListQuery,
  ChangeMetrics,
  ChangeSummary,
  ChangeTargetStatus,
  ChangeTemplate,
  ClassificationInput,
  ClassificationResult,
  CreateChangeInput,
  CreatedChange,
  LinkInput,
  PageResponse,
  ResultInput,
  ScheduleItem,
} from "@/features/change/types";

/* CHG API 호출 — 모두 공통 apiClient 경유. api_spec/change.md 준수. */

function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") params[key] = value;
  }
  return params;
}

export const changeApi = {
  // API-CHG-001 목록
  async list(query: ChangeListQuery): Promise<PageResponse<ChangeSummary>> {
    const res = await apiClient.get<PageResponse<ChangeSummary>>("/changes", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-CHG-002 RFC 생성
  async create(body: CreateChangeInput): Promise<CreatedChange> {
    const res = await apiClient.post<CreatedChange>("/changes", body);
    return res.data;
  },

  // API-CHG-003 상세
  async get(id: number): Promise<ChangeDetail> {
    const res = await apiClient.get<ChangeDetail>(`/changes/${id}`);
    return res.data;
  },

  // API-CHG-004 6단계 상태 전이
  async transition(id: number, targetStatus: ChangeTargetStatus, note?: string): Promise<{ id: number; status: string }> {
    const res = await apiClient.patch<{ id: number; status: string }>(
      `/changes/${id}/status`,
      { targetStatus, note },
    );
    return res.data;
  },

  // API-CHG-005 유형·위험 변경
  async updateClassification(id: number, body: ClassificationInput): Promise<ClassificationResult> {
    const res = await apiClient.patch<ClassificationResult>(`/changes/${id}/classification`, body);
    return res.data;
  },

  // API-CHG-008 구현 결과 기록
  async recordResult(id: number, body: ResultInput): Promise<void> {
    await apiClient.post(`/changes/${id}/result`, body);
  },

  // API-CHG-009 인시던트/문제 연계
  async link(id: number, body: LinkInput): Promise<void> {
    await apiClient.post(`/changes/${id}/links`, body);
  },

  // API-CHG-010 변경 일정(캘린더) 조회
  async schedule(params: { from?: string; to?: string; type?: string } = {}): Promise<ScheduleItem[]> {
    const res = await apiClient.get<ScheduleItem[]>("/changes/schedule", {
      params: cleanParams(params),
    });
    return res.data;
  },

  // API-CHG-011 표준 변경 템플릿 목록
  async listTemplates(): Promise<ChangeTemplate[]> {
    const res = await apiClient.get<ChangeTemplate[]>("/change-templates");
    return res.data;
  },

  // API-CHG-012 변경 지표
  async metrics(params: { from?: string; to?: string } = {}): Promise<ChangeMetrics> {
    const res = await apiClient.get<ChangeMetrics>("/changes/metrics", {
      params: cleanParams(params),
    });
    return res.data;
  },
};
