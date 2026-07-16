import { apiClient } from "@/lib/apiClient";
import type {
  AssigneeCandidate,
  Category,
  CatalogItemDetail,
  CatalogItemInput,
  CatalogItemSummary,
  CreateRequestInput,
  CreatedRequest,
  KnowledgeSuggestion,
  PageResponse,
  Queue,
  RequestComment,
  RequestDetail,
  RequestListQuery,
  RequestMetrics,
  RequestSummary,
  TargetStatus,
} from "@/features/service-request/types";

/* SRM API 호출 — 모두 공통 apiClient 경유. api_spec/service-request.md 준수. */

function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") params[key] = value;
  }
  return params;
}

export const srmApi = {
  // API-SRM-001 카탈로그 목록
  async listCatalog(params: { categoryId?: number; keyword?: string } = {}): Promise<CatalogItemSummary[]> {
    const res = await apiClient.get<CatalogItemSummary[]>("/service-catalog/items", {
      params: cleanParams(params),
    });
    return res.data;
  },

  // API-SRM-002 카탈로그 항목 상세(양식 스키마)
  async getCatalogItem(id: number): Promise<CatalogItemDetail> {
    const res = await apiClient.get<CatalogItemDetail>(`/service-catalog/items/${id}`);
    return res.data;
  },

  // API-SRM-003 카탈로그 항목 생성 (Process Owner)
  async createCatalogItem(body: CatalogItemInput): Promise<CatalogItemDetail> {
    const res = await apiClient.post<CatalogItemDetail>("/service-catalog/items", body);
    return res.data;
  },

  // API-SRM-004 카탈로그 항목 수정 (Process Owner)
  async updateCatalogItem(id: number, body: Partial<CatalogItemInput>): Promise<CatalogItemDetail> {
    const res = await apiClient.patch<CatalogItemDetail>(`/service-catalog/items/${id}`, body);
    return res.data;
  },

  // API-SRM-005 지식 기사 추천 (KM 미구축 시 빈 배열)
  async suggestions(params: { catalogItemId?: number; keyword?: string }): Promise<KnowledgeSuggestion[]> {
    const res = await apiClient.get<KnowledgeSuggestion[]>("/knowledge/suggestions", {
      params: cleanParams(params),
    });
    return res.data;
  },

  // API-SRM-006 요청 생성(제출)
  async createRequest(body: CreateRequestInput): Promise<CreatedRequest> {
    const res = await apiClient.post<CreatedRequest>("/service-requests", body);
    return res.data;
  },

  // API-SRM-016 큐 목록(건수 포함, Agent 이상)
  async listQueues(): Promise<Queue[]> {
    const res = await apiClient.get<Queue[]>("/queues");
    return res.data;
  },

  // API-SRM-007 요청 목록
  async listRequests(query: RequestListQuery): Promise<PageResponse<RequestSummary>> {
    const res = await apiClient.get<PageResponse<RequestSummary>>("/service-requests", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-SRM-008 요청 상세
  async getRequest(id: number): Promise<RequestDetail> {
    const res = await apiClient.get<RequestDetail>(`/service-requests/${id}`);
    return res.data;
  },

  // API-SRM-009 담당자 배정 (Agent). 미지정 시 본인.
  async assign(id: number, assigneeId?: number): Promise<void> {
    await apiClient.post(`/service-requests/${id}/assign`, assigneeId ? { assigneeId } : {});
  },

  // API-SRM-017 요청 담당자 후보 목록 조회 (Agent)
  async getAssigneeCandidates(id: number): Promise<AssigneeCandidate[]> {
    const res = await apiClient.get<AssigneeCandidate[]>(`/service-requests/${id}/assignee-candidates`);
    return res.data;
  },

  // API-SRM-010 상태 전이
  async transition(id: number, targetStatus: TargetStatus, note?: string): Promise<{ id: number; status: string }> {
    const res = await apiClient.patch<{ id: number; status: string }>(
      `/service-requests/${id}/status`,
      { targetStatus, note },
    );
    return res.data;
  },

  // API-SRM-013 코멘트 등록
  async addComment(id: number, body: string): Promise<RequestComment> {
    const res = await apiClient.post<RequestComment>(`/service-requests/${id}/comments`, { body });
    return res.data;
  },

  // API-SRM-014 CSAT 제출 (요청자, 종료된 요청)
  async submitCsat(id: number, score: number, comment?: string): Promise<{ id: number; score: number }> {
    const res = await apiClient.post<{ id: number; score: number }>(
      `/service-requests/${id}/csat`,
      { score, comment },
    );
    return res.data;
  },

  // API-SRM-015 지표 (Agent 이상)
  async metrics(params: { from?: string; to?: string } = {}): Promise<RequestMetrics> {
    const res = await apiClient.get<RequestMetrics>("/service-requests/metrics", {
      params: cleanParams(params),
    });
    return res.data;
  },

  // API-SRM-018 카탈로그 카테고리 목록(인증만)
  async listCategories(): Promise<Category[]> {
    const res = await apiClient.get<Category[]>("/service-catalog/categories");
    return res.data;
  },

  // API-SRM-019 카탈로그 카테고리 생성 (Process Owner)
  async createCategory(body: { name: string; sortOrder?: number }): Promise<Category> {
    const res = await apiClient.post<Category>("/service-catalog/categories", body);
    return res.data;
  },

  // API-SRM-020 카탈로그 카테고리 수정 (Process Owner)
  async updateCategory(id: number, body: { name?: string; sortOrder?: number }): Promise<Category> {
    const res = await apiClient.patch<Category>(`/service-catalog/categories/${id}`, body);
    return res.data;
  },

  // API-SRM-021 카탈로그 카테고리 삭제 (Process Owner)
  async deleteCategory(id: number): Promise<void> {
    await apiClient.delete(`/service-catalog/categories/${id}`);
  },
};
