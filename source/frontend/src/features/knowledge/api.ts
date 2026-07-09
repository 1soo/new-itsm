import { apiClient } from "@/lib/apiClient";
import type {
  ArticleDetail,
  ArticleInput,
  ArticleListQuery,
  ArticleStatus,
  ArticleSummary,
  Category,
  CreatedArticle,
  Decision,
  FeedbackInput,
  FeedbackResult,
  KnowledgeMetrics,
  LinkInput,
  PageResponse,
  ReviewQueueItem,
} from "@/features/knowledge/types";

/* KM API 호출 — 모두 공통 apiClient 경유. api_spec/knowledge.md 준수. */

function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") params[key] = value;
  }
  return params;
}

export const knowledgeApi = {
  // API-KM-001 기사 검색/목록
  async list(query: ArticleListQuery): Promise<PageResponse<ArticleSummary>> {
    const res = await apiClient.get<PageResponse<ArticleSummary>>("/knowledge/articles", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-KM-002 기사 상세/열람
  async get(id: number): Promise<ArticleDetail> {
    const res = await apiClient.get<ArticleDetail>(`/knowledge/articles/${id}`);
    return res.data;
  },

  // API-KM-003 기사 작성 (Contributor)
  async create(body: ArticleInput): Promise<CreatedArticle> {
    const res = await apiClient.post<CreatedArticle>("/knowledge/articles", body);
    return res.data;
  },

  // API-KM-004 기사 수정 (Contributor)
  async update(id: number, body: Partial<ArticleInput>): Promise<void> {
    await apiClient.patch(`/knowledge/articles/${id}`, body);
  },

  // API-KM-005 기사 삭제 (Contributor)
  async remove(id: number): Promise<void> {
    await apiClient.delete(`/knowledge/articles/${id}`);
  },

  // API-KM-006 기사 상태 전이(검토 요청)
  async transition(id: number, targetStatus: Extract<ArticleStatus, "IN_REVIEW">): Promise<void> {
    await apiClient.patch(`/knowledge/articles/${id}/status`, { targetStatus });
  },

  // API-KM-007 검토·게시 승인/반려 (Gatekeeper)
  async review(id: number, decision: Decision, reason?: string): Promise<{ id: number; status: ArticleStatus }> {
    const res = await apiClient.post<{ id: number; status: ArticleStatus }>(
      `/knowledge/articles/${id}/review`,
      { decision, reason },
    );
    return res.data;
  },

  // API-KM-008 검토 대기 목록 (Gatekeeper)
  async listReviews(): Promise<ReviewQueueItem[]> {
    const res = await apiClient.get<ReviewQueueItem[]>("/knowledge/reviews", {
      params: { scope: "mine" },
    });
    return res.data;
  },

  // API-KM-009 유용성 평가/피드백
  async submitFeedback(id: number, body: FeedbackInput): Promise<FeedbackResult> {
    const res = await apiClient.post<FeedbackResult>(`/knowledge/articles/${id}/feedback`, body);
    return res.data;
  },

  // API-KM-010 카테고리 목록
  async listCategories(): Promise<Category[]> {
    const res = await apiClient.get<Category[]>("/knowledge/categories");
    return res.data;
  },

  // API-KM-011 KCS 티켓 연계(작성/연결)
  async link(body: LinkInput): Promise<{ articleId: number; ticketId: number }> {
    const res = await apiClient.post<{ articleId: number; ticketId: number }>(
      "/knowledge/articles/link",
      body,
    );
    return res.data;
  },

  // API-KM-012 지식 지표
  async metrics(params: { from?: string; to?: string } = {}): Promise<KnowledgeMetrics> {
    const res = await apiClient.get<KnowledgeMetrics>("/knowledge/metrics", {
      params: cleanParams(params),
    });
    return res.data;
  },
};
