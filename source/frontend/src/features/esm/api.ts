import { apiClient } from "@/lib/apiClient";
import type {
  CatalogItemDetail,
  CatalogItemInput,
  CatalogItemSummary,
  ChecklistDetail,
  ChecklistTaskStatus,
  CreateEsmRequestInput,
  CreatedEsmRequest,
  CreateHrCaseInput,
  CreatedHrCase,
  Department,
  EsmComment,
  EsmMetrics,
  EsmRequestDetail,
  EsmRequestListQuery,
  EsmRequestSummary,
  EsmRequestTargetStatus,
  HrCaseDetail,
  HrCaseSummary,
  HrCaseTargetStatus,
  MyChecklistTask,
  PageResponse,
} from "@/features/esm/types";

/* ESM API 호출 — 모두 공통 apiClient 경유. api_spec/esm.md 준수. */

function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") params[key] = value;
  }
  return params;
}

export const esmApi = {
  // API-ESM-001 부서 카탈로그 목록 조회
  async listCatalog(params: { department?: Department | ""; keyword?: string } = {}): Promise<CatalogItemSummary[]> {
    const res = await apiClient.get<CatalogItemSummary[]>("/esm/catalog-items", {
      params: cleanParams(params),
    });
    return res.data;
  },

  // API-ESM-002 카탈로그 항목 상세(양식 스키마)
  async getCatalogItem(id: number): Promise<CatalogItemDetail> {
    const res = await apiClient.get<CatalogItemDetail>(`/esm/catalog-items/${id}`);
    return res.data;
  },

  // API-ESM-003 카탈로그 항목 생성 (Process Owner)
  async createCatalogItem(body: CatalogItemInput): Promise<{ id: number }> {
    const res = await apiClient.post<{ id: number }>("/esm/catalog-items", body);
    return res.data;
  },

  // API-ESM-004 카탈로그 항목 수정 (Process Owner)
  async updateCatalogItem(id: number, body: Partial<CatalogItemInput>): Promise<void> {
    await apiClient.patch(`/esm/catalog-items/${id}`, body);
  },

  // API-ESM-005 부서 요청 제출
  async createRequest(body: CreateEsmRequestInput): Promise<CreatedEsmRequest> {
    const res = await apiClient.post<CreatedEsmRequest>("/esm/requests", body);
    return res.data;
  },

  // API-ESM-006 부서 요청 목록 조회
  async listRequests(query: EsmRequestListQuery): Promise<PageResponse<EsmRequestSummary>> {
    const res = await apiClient.get<PageResponse<EsmRequestSummary>>("/esm/requests", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-ESM-007 부서 요청 상세 조회
  async getRequest(id: number): Promise<EsmRequestDetail> {
    const res = await apiClient.get<EsmRequestDetail>(`/esm/requests/${id}`);
    return res.data;
  },

  // API-ESM-008 부서 요청 상태 전이
  async transition(id: number, targetStatus: EsmRequestTargetStatus, note?: string): Promise<{ id: number; status: string }> {
    const res = await apiClient.patch<{ id: number; status: string }>(
      `/esm/requests/${id}/status`,
      { targetStatus, note },
    );
    return res.data;
  },

  // API-ESM-009 부서 요청 코멘트 등록
  async addComment(id: number, body: string): Promise<EsmComment> {
    const res = await apiClient.post<EsmComment>(`/esm/requests/${id}/comments`, { body });
    return res.data;
  },

  // API-ESM-010 HR 케이스 접수
  async createHrCase(body: CreateHrCaseInput): Promise<CreatedHrCase> {
    const res = await apiClient.post<CreatedHrCase>("/esm/hr-cases", body);
    return res.data;
  },

  // API-ESM-011 HR 케이스 목록 조회
  async listHrCases(params: { status?: string; page?: number; size?: number } = {}): Promise<PageResponse<HrCaseSummary>> {
    const res = await apiClient.get<PageResponse<HrCaseSummary>>("/esm/hr-cases", {
      params: cleanParams(params),
    });
    return res.data;
  },

  // API-ESM-012 HR 케이스 상세 조회
  async getHrCase(id: number): Promise<HrCaseDetail> {
    const res = await apiClient.get<HrCaseDetail>(`/esm/hr-cases/${id}`);
    return res.data;
  },

  // API-ESM-013 HR 케이스 상태 전이
  async transitionHrCase(id: number, targetStatus: HrCaseTargetStatus, note?: string): Promise<void> {
    await apiClient.patch(`/esm/hr-cases/${id}/status`, { targetStatus, note });
  },

  // API-ESM-014 체크리스트 상세 조회
  async getChecklist(id: number): Promise<ChecklistDetail> {
    const res = await apiClient.get<ChecklistDetail>(`/esm/checklists/${id}`);
    return res.data;
  },

  // API-ESM-015 내 하위 작업 목록 조회
  async listMyChecklistTasks(params: { status?: ChecklistTaskStatus | ""; page?: number; size?: number } = {}): Promise<PageResponse<MyChecklistTask>> {
    const res = await apiClient.get<PageResponse<MyChecklistTask>>("/esm/checklist-tasks", {
      params: cleanParams({ scope: "mine", ...params }),
    });
    return res.data;
  },

  // API-ESM-016 하위 작업 상태 변경
  async completeChecklistTask(taskId: number): Promise<{ id: number; status: string; checklistStatus: string }> {
    const res = await apiClient.patch<{ id: number; status: string; checklistStatus: string }>(
      `/esm/checklist-tasks/${taskId}/status`,
      { status: "DONE" },
    );
    return res.data;
  },

  // API-ESM-017 ESM 지표 조회
  async metrics(params: { from?: string; to?: string; department?: Department | "" } = {}): Promise<EsmMetrics> {
    const res = await apiClient.get<EsmMetrics>("/esm/metrics", { params: cleanParams(params) });
    return res.data;
  },
};
