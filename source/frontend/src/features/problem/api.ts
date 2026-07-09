import { apiClient } from "@/lib/apiClient";
import type {
  ActionInput,
  ActionStatus,
  CloseResult,
  CreateProblemInput,
  CreatedKnownError,
  CreatedProblem,
  KnownError,
  KnownErrorInput,
  LinkInput,
  PageResponse,
  ProblemAction,
  ProblemDetail,
  ProblemListQuery,
  ProblemSummary,
  ProblemTargetStatus,
  Rca,
  WorkaroundInput,
} from "@/features/problem/types";

/* PRB API 호출 — 모두 공통 apiClient 경유. api_spec/problem.md 준수. */

function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") params[key] = value;
  }
  return params;
}

export const problemApi = {
  // API-PRB-001 목록
  async list(query: ProblemListQuery): Promise<PageResponse<ProblemSummary>> {
    const res = await apiClient.get<PageResponse<ProblemSummary>>("/problems", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-PRB-002 등록
  async create(body: CreateProblemInput): Promise<CreatedProblem> {
    const res = await apiClient.post<CreatedProblem>("/problems", body);
    return res.data;
  },

  // API-PRB-003 상세
  async get(id: number): Promise<ProblemDetail> {
    const res = await apiClient.get<ProblemDetail>(`/problems/${id}`);
    return res.data;
  },

  // API-PRB-004 6단계 상태 전이
  async transition(id: number, targetStatus: ProblemTargetStatus, note?: string): Promise<void> {
    await apiClient.patch(`/problems/${id}/status`, { targetStatus, note });
  },

  // API-PRB-005 RCA 작성/수정
  async saveRca(id: number, body: Rca): Promise<Rca> {
    const res = await apiClient.put<Rca>(`/problems/${id}/rca`, body);
    return res.data;
  },

  // API-PRB-006 워크어라운드 등록
  async addWorkaround(id: number, body: WorkaroundInput): Promise<void> {
    await apiClient.post(`/problems/${id}/workaround`, body);
  },

  // API-PRB-007 알려진 오류(KE) 생성
  async createKnownError(id: number, body: KnownErrorInput): Promise<CreatedKnownError> {
    const res = await apiClient.post<CreatedKnownError>(`/problems/${id}/known-errors`, body);
    return res.data;
  },

  // API-PRB-008 KEDB 검색
  async searchKnownErrors(
    params: { keyword?: string; page?: number; size?: number } = {},
  ): Promise<PageResponse<KnownError>> {
    const res = await apiClient.get<PageResponse<KnownError>>("/known-errors", {
      params: cleanParams(params),
    });
    return res.data;
  },

  // API-PRB-009 인시던트/변경 연계
  async link(id: number, body: LinkInput): Promise<void> {
    await apiClient.post(`/problems/${id}/links`, body);
  },

  // API-PRB-010 후속 조치 등록
  async addAction(id: number, body: ActionInput): Promise<ProblemAction> {
    const res = await apiClient.post<ProblemAction>(`/problems/${id}/actions`, body);
    return res.data;
  },

  // API-PRB-011 후속 조치 상태 변경
  async updateActionStatus(id: number, actionId: number, status: ActionStatus): Promise<void> {
    await apiClient.patch(`/problems/${id}/actions/${actionId}`, { status });
  },

  // API-PRB-012 문제 종료 (미해결 후속조치 있으면 warning; force=true 시 강제 종료)
  async close(id: number, force = false): Promise<CloseResult> {
    const res = await apiClient.post<CloseResult>(`/problems/${id}/close`, { force });
    return res.data;
  },
};
